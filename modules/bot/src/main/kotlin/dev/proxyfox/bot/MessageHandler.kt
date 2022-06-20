package dev.proxyfox.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import dev.proxyfox.bot.string.parser.parseString
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.common.prefixRegex
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode

suspend fun MessageCreateEvent.onMessageCreate() {
    // Return if bot
    if (message.webhookId != null || message.author!!.isBot) return

    // Get message content to check with regex
    val content = message.content
    if (prefixRegex.matches(content)) {
        // Remove the prefix to pass into dispatcher
        val contentWithoutRegex = content.substring(3)
        // Run the command
        val output = parseString(contentWithoutRegex, message) ?: return
        // Send output message if exists
        if (output.isNotBlank())
            message.channel.createMessage(output)
    } else {
        // Proxy the message
        val proxy = database.getProxyTagFromMessage(message.author!!.id.value.toString(), content)
        if (proxy != null) {
            val member = database.getMemberById(proxy.systemId, proxy.memberId)!!
            val server = database.getServerSettings(message.getGuild().id.toString())
            for (role in message.author!!.asMember(message.getGuild().id).roleIds)
                if (role.toString() == server.proxyRole) return

            val systemServer = database.getServerSettingsById(message.getGuild().id.toString(), member.systemId)
            if (!systemServer.proxyEnabled || systemServer.autoProxyMode == AutoProxyMode.OFF) return
            val systemChannel = database.getChannelSettings(message.getGuild().id.toString(), member.systemId)
            if (!systemChannel.proxyEnabled) return
            val memberServer = database.getMemberServerSettingsById(message.getGuild().id.toString(), member.systemId, member.id)
            if (!memberServer!!.proxyEnabled) return
            WebhookUtil.prepareMessage(message, member, proxy).send()
        }
    }
}