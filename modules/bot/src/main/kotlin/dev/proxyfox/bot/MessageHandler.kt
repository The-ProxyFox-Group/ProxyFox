package dev.proxyfox.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.proxyfox.bot.string.parser.parseString
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.common.prefixRegex
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("MessageHandler")

suspend fun MessageCreateEvent.onMessageCreate() {
    val user = message.author ?: return
    val channel = message.getChannel()

    // Return if bot
    if (message.webhookId != null || user.isBot) return

    // Get message content to check with regex
    val content = message.content
    if (prefixRegex.matches(content)) {
        // Remove the prefix to pass into dispatcher
        val contentWithoutRegex = content.substring(3)
        // Run the command
        val output = parseString(contentWithoutRegex, message) ?: return
        // Send output message if exists
        if (output.isNotBlank())
            channel.createMessage(output)
    } else if (channel is GuildMessageChannel) {
        val guild = channel.getGuild()
        val hasStickers = message.stickers.isNotEmpty()
        val hasOversizedFiles = message.attachments.any { it.size >= UPLOAD_LIMIT }
        val isOversizedMessage = message.content.length > 2000
        if (hasStickers || hasOversizedFiles || isOversizedMessage) {
            logger.trace("Denying proxying {} ({}) in {} ({}) due to Discord bot constraints", user.tag, user.id, guild.name, guild.id)
            return
        }

        val userId = user.id.value

        val server = database.getServerSettings(guild)
        server.proxyRole.let {
            if (it != 0UL && !user.asMember(guild.id).roleIds.contains(Snowflake(it))) {
                logger.trace("Denying proxying {} ({}) in {} ({}) due to missing role {}", user.tag, user.id, guild.name, guild.id, it)
                return
            }
        }

        val system = database.getSystemByHost(userId) ?: return

        val systemChannel = database.getChannelSettings(channel, system.id)
        if (!systemChannel.proxyEnabled) return

        val systemServer = database.getServerSettingsById(guild, system.id)
        if (!systemServer.proxyEnabled) return

        // Proxy the message
        val proxy = database.getProxyTagFromMessage(message.author, content)
        if (proxy != null) {
            val member = database.getMemberById(proxy.systemId, proxy.memberId)!!

            // Respect member settings.
            val memberServer = database.getMemberServerSettingsById(guild, system.id, member.id)
            if (memberServer?.proxyEnabled == false) return

            if (systemServer.autoProxyMode == AutoProxyMode.LATCH) {
                systemServer.autoProxy = proxy.memberId
                database.updateSystemServerSettings(systemServer)
            } else if (systemServer.autoProxyMode == AutoProxyMode.FALLBACK && system.autoType == AutoProxyMode.LATCH) {
                system.autoProxy = proxy.memberId
                database.updateSystem(system)
            }

            WebhookUtil.prepareMessage(message, member, proxy).send()
        } else if (content.startsWith('\\')) {
            // Doesn't proxy just for this message.
            if (content.startsWith("\\\\")) {
                // Break latch
                if (systemServer.autoProxyMode == AutoProxyMode.LATCH) {
                    systemServer.autoProxy = null
                    database.updateSystemServerSettings(systemServer)
                } else if (systemServer.autoProxyMode == AutoProxyMode.FALLBACK && system.autoType == AutoProxyMode.LATCH) {
                    system.autoProxy = null
                    database.updateSystem(system)
                }
            }
        } else {
            // Allows AutoProxy to be disabled at a server level.
            if (systemServer.autoProxyMode == AutoProxyMode.OFF) return
            val memberId = if (systemServer.autoProxyMode == AutoProxyMode.FALLBACK) system.autoProxy else systemServer.autoProxy
            val member = database.getMemberById(system.id, memberId ?: return) ?: return

            WebhookUtil.prepareMessage(message, member, null).send()
        }
    }
}