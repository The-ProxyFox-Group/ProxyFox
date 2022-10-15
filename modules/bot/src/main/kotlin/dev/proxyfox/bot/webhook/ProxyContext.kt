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
import dev.kord.core.entity.User
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.request.KtorRequestException
import dev.proxyfox.bot.http
import dev.proxyfox.bot.kord
import dev.proxyfox.bot.md.BaseMarkdown
import dev.proxyfox.bot.md.MarkdownString
import dev.proxyfox.bot.md.parseMarkdown
import dev.proxyfox.common.ellipsis
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.delay

/**
 * Context for proxying
 * @author Oliver
 * */
@JvmRecord
data class ProxyContext(
    val messageContent: String,
    val webhook: WebhookHolder,
    val message: Message,
    val system: SystemRecord,
    val member: MemberRecord,
    val proxy: MemberProxyTagRecord?,
    val threadId: Snowflake?,
    val resolvedUsername: String,
    val resolvedAvatar: String?,
    val moderationDelay: Long,
) {
    @OptIn(InternalAPI::class)
    suspend fun send(reproxy: Boolean = false) {
        val newMessage = webhook.execute(threadId) {
            if (messageContent.isNotBlank()) content = messageContent
            username = resolvedUsername + " " + (system.tag ?: "")
            avatarUrl = resolvedAvatar
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
            } else message.referencedMessage?.let { ref ->
                // Kord's official methods don't return a user if it's a webhook
                val user = User(ref.data.author, kord)
                val link = "https://discord.com/channels/${ref.getGuild().id}/${ref.channelId}/${ref.id}"
                embed {
                    color = Color(member.color)
                    author {
                        name = (ref.getAuthorAsMember()?.displayName ?: user.username) + " ↩️"
                        icon = user.avatar?.url ?: user.defaultAvatar.url
                        url = link
                    }
                    var msgRef = parseMarkdown(ref.content)
                    if (msgRef.length > 100) {
                        // We know it's gonna be a BaseMarkdown so
                        msgRef = msgRef.substring(100) as BaseMarkdown
                        msgRef.values.add(MarkdownString(ellipsis))
                    }
                    description = "[**Reply to:**]($link) $msgRef"
                }
            }
        }
        if (newMessage.content != messageContent && messageContent.isNotBlank())
            webhook.edit(newMessage.id, threadId) {
                content = messageContent
            }
        member.messageCount++
        database.updateMember(member)
        val userId = if (reproxy)
            Snowflake(database.fetchMessage(message.id)!!.userId)
        else message.author!!.id
        database.createMessage(userId, message.id, newMessage.id, message.channel, member.id, member.systemId, resolvedUsername)
        delay(moderationDelay)
        try {
            message.delete()
        } catch (e: KtorRequestException) {
            if (e.httpResponse.status == HttpStatusCode.NotFound) {
                try {
                    webhook.delete(newMessage.id, threadId)
                } catch (e2: KtorRequestException) {
                    if (e2.httpResponse.status != HttpStatusCode.NotFound) {
                        e2.addSuppressed(e)
                        throw e2
                    }
                }
            } else throw e
        }
    }
}