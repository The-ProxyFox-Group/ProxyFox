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
import dev.kord.core.entity.Message
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
    val proxy: MemberProxyTagRecord?
) {
    @OptIn(InternalAPI::class)
    suspend fun send() {
        if (!member.keepProxy && proxy != null)
            messageContent = proxy.trim(messageContent)
        val serverMember = database.getMemberServerSettingsById(
            message.getGuildOrNull(),
            member.systemId,
            member.id
        ) ?: MemberServerSettingsRecord()
        val newMessage = kord.rest.webhook.executeWebhook(Snowflake(webhook.id), webhook.token!!, wait = true /* REQUIRED */) {
            if (messageContent.isNotBlank()) content = messageContent
            username = (serverMember.nickname ?: member.displayName ?: member.name) + " " + (system.tag ?: "")
            avatarUrl = (serverMember.avatarUrl ?: member.avatarUrl ?: system.avatarUrl ?: "")
            for (attachment in message.attachments) {
                val response: HttpResponse = http.get(urlString = attachment.url) {
                    headers { append(HttpHeaders.UserAgent, "ProxyFox/2.0.0 (+https://github.com/ProxyFox-Developers/ProxyFox/; +https://proxyfox.dev/)") }
                }
                files.add(NamedFile(attachment.filename, response.content.toInputStream()))
            }
            if (message.referencedMessage != null) {
                val ref = message.referencedMessage!!
                embed {
                    color = Color(member.color)
                    field {
                        name = ref.author!!.username + " \\↩"
                        var msgRef = parseMarkdown(ref.content)
                        if (msgRef.length > 100) {
                            // We know it's gonna be a BaseMarkdown so
                            msgRef = msgRef.substring(100) as BaseMarkdown
                            msgRef.values.add(MarkdownString("..."))
                        }
                        value = "[**Reply to:**](https://discord.com/channels/${ref.getGuild().id}/${ref.channelId}/${ref.id}) $msgRef"
                    }
                }
            }
        }!!
        database.createMessage(message.id, newMessage.id, newMessage.channelId, member.id, member.systemId)
        message.delete()
    }
}