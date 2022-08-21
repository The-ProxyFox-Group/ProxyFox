package dev.proxyfox.bot.webhook

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.bot.http
import dev.proxyfox.bot.kord
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
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
    val member: MemberRecord,
    val proxy: MemberProxyTagRecord?
) {
    @OptIn(InternalAPI::class)
    suspend fun send() {
        if (!member.keepProxy && proxy != null)
            messageContent = proxy.trim(messageContent)
        val system = database.getSystemById(member.systemId)!!
        val serverMember = database.getMemberServerSettingsById(
            message.getGuildOrNull()!!.id.value.toString(),
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
                        name = ref.author!!.username + " ↩"
                        value = "[**Reply to:**](https://discord.com/channels/${ref.getGuild().id}/${ref.channelId}/${ref.id}) ${ref.content.substring(0, 100)}"
                    }

                }
            }
            return@executeWebhook
        }!!
        database.createMessage(message.id, newMessage.id, member.id, member.systemId)
        message.delete()
    }
}