/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.bot.http
import dev.proxyfox.bot.kord
import dev.proxyfox.bot.md.BaseMarkdown
import dev.proxyfox.bot.md.MarkdownNode
import dev.proxyfox.bot.md.MarkdownString
import dev.proxyfox.bot.md.parseMarkdown
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*

/**
 * Context for proxying
 * @author Oliver
 * */
data class ProxyContext(
    var messageContent: String,
    val webhook: WebhookHolder,
    val message: Message,
    val system: SystemRecord,
    val member: MemberRecord,
    val proxy: MemberProxyTagRecord?,
    val threadId: Snowflake?
) {
    @OptIn(InternalAPI::class)
    suspend fun send(reproxy: Boolean = false) {
        if (!member.keepProxy && proxy != null)
            messageContent = proxy.trim(messageContent).trim()
        if (messageContent.isBlank()) return
        val serverMember = database.getMemberServerSettingsById(
            message.getGuildOrNull(),
            member.systemId,
            member.id
        ) ?: MemberServerSettingsRecord()
        val newMessage = kord.rest.webhook.executeWebhook(Snowflake(webhook.id), webhook.token!!, true, threadId) {
            if (messageContent.isNotBlank()) content = messageContent
            username = (serverMember.nickname ?: member.displayName ?: member.name) + " " + (system.tag ?: "")
            avatarUrl = (serverMember.avatarUrl ?: member.avatarUrl ?: system.avatarUrl ?: "")
            for (attachment in message.attachments) {
                val response: HttpResponse = http.get(urlString = attachment.url) {
                    headers { append(HttpHeaders.UserAgent, "ProxyFox/2.0.0 (+https://github.com/ProxyFox-Developers/ProxyFox/; +https://proxyfox.dev/)") }
                }
                files.add(NamedFile(attachment.filename, response.content.toInputStream()))
            }
            if (reproxy) {
                message.embeds.forEach {
                    if (it.author?.name?.endsWith(" ↩️") == true) {
                        embed {
                            color = Color(member.color)
                            author {
                                name = it.author?.name
                                icon = it.author?.iconUrl
                            }
                            description = it.description
                        }
                    }
                }
            }
            if (message.referencedMessage != null) {
                val ref = message.referencedMessage!!
                // Kord's official methods don't return a user if it's a webhook
                val user = User(ref.data.author, kord)
                embed {
                    color = Color(member.color)
                    author {
                        name = user.username + " ↩️"
                        icon = user.avatar?.url ?: user.defaultAvatar.url
                    }
                    var msgRef = parseMarkdown(ref.content)
                    if (msgRef.length > 100) {
                        // We know it's gonna be a BaseMarkdown so
                        msgRef = msgRef.substring(100) as BaseMarkdown
                        msgRef.values.add(MarkdownString("..."))
                    }
                    description = "[**Reply to:**](https://discord.com/channels/${ref.getGuild().id}/${ref.channelId}/${ref.id}) $msgRef"
                }
            }
        }!!
        member.messageCount++
        database.updateMember(member)
        val userId = if (reproxy)
            Snowflake(database.fetchMessage(message.id)!!.userId)
        else message.author!!.id
        database.createMessage(userId, message.id, newMessage.id, message.channel, member.id, member.systemId, serverMember.nickname ?: member.displayName ?: member.name)
        message.delete()
    }
}