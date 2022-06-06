package dev.proxyfox.bot.webhook

import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.toRawType
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.bot.kord

/**
 * Context for proxying
 * @author Oliver
 * */
data class ProxyContext(
    var messageContent: String,
    var attachments: List<DiscordAttachment>,
    var webhook: WebhookHolder,
    var message: Message,
    var member: MemberRecord,
    var proxy: MemberProxyTagRecord
) {
    suspend fun send() {
        if (!member.keepProxy)
            messageContent = proxy.trim(messageContent)
        val system = database.getSystemById(member.systemId)!!
        val serverMember = database.getMemberServerSettingsById(
            message.getGuildOrNull()!!.id.value.toString(),
            member.systemId,
            member.id
        ) ?: MemberServerSettingsRecord()
        kord.rest.webhook.executeWebhook(Snowflake(webhook.id), webhook.token!!, false) {
            if (messageContent.isNotBlank()) content = messageContent
            username = (serverMember.nickname ?: member.displayName ?: member.name) + " " + (system.tag ?: "")
            avatarUrl = (serverMember.avatarUrl ?: member.avatarUrl ?: system.avatarUrl ?: "")
            attachments = ArrayList()
            for (attachment in message.attachments)
                (attachments as ArrayList<DiscordAttachment>).add(attachment.toRawType())
            return@executeWebhook
        }
        message.delete()
    }
}