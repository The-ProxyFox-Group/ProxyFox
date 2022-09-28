/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.bot.string.parser.parseString
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.common.ellipsis
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode
import org.slf4j.LoggerFactory

val prefixRegex = Regex("^(?:(<@!?${kord.selfId}>)|pf[>;!:])\\s*", RegexOption.IGNORE_CASE)

private val logger = LoggerFactory.getLogger("MessageHandler")

suspend fun MessageCreateEvent.onMessageCreate() {
    val user = message.author ?: return
    val channel = message.getChannel()

    // Return if bot
    if (message.webhookId != null || user.isBot) return

    // Get message content to check with regex
    val content = message.content
    val matcher = prefixRegex.toPattern().matcher(content)
    if (matcher.find()) {
        // Remove the prefix to pass into dispatcher
        val contentWithoutRegex = content.substring(matcher.end())

        if (contentWithoutRegex.isBlank() && matcher.start(1) >= 0) {
            channel.createMessage("Hi, I'm ProxyFox! My prefix is `pf>`.")
        } else {
            // Run the command
            val output = parseString(contentWithoutRegex, message) ?: return
            // Send output message if exists
            if (output.isNotBlank())
                channel.createMessage(output)
        }
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

        val server = database.getOrCreateServerSettings(guild)
        server.proxyRole.let {
            if (it != 0UL && !user.asMember(guild.id).roleIds.contains(Snowflake(it))) {
                logger.trace("Denying proxying {} ({}) in {} ({}) due to missing role {}", user.tag, user.id, guild.name, guild.id, it)
                return
            }
        }

        val system = database.fetchSystemFromUser(userId) ?: return

        val systemChannelSettings = database.getOrCreateChannelSettingsFromSystem(channel, system.id)
        if (!systemChannelSettings.proxyEnabled) return

        val systemServerSettings = database.getOrCreateServerSettingsFromSystem(guild, system.id)
        if (!systemServerSettings.proxyEnabled) return

        val channelSettings = database.getOrCreateChannel(guild.id.value, channel.id.value)
        if (!channelSettings.proxyEnabled) return

        // Proxy the message
        val proxy = database.fetchProxyTagFromMessage(message.author, content)
        if (proxy != null) {
            val member = database.fetchMemberFromSystem(proxy.systemId, proxy.memberId)!!

            // Respect member settings.
            val memberServer = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)
            if (memberServer?.proxyEnabled == false) return

            if (systemServerSettings.autoProxyMode == AutoProxyMode.LATCH) {
                systemServerSettings.autoProxy = proxy.memberId
                database.updateSystemServerSettings(systemServerSettings)
            } else if (systemServerSettings.autoProxyMode == AutoProxyMode.FALLBACK && system.autoType == AutoProxyMode.LATCH) {
                system.autoProxy = proxy.memberId
                database.updateSystem(system)
            }

            WebhookUtil.prepareMessage(message, system, member, proxy).send()
        } else if (content.startsWith('\\')) {
            // Doesn't proxy just for this message.
            if (content.startsWith("\\\\")) {
                // Break latch
                if (systemServerSettings.autoProxyMode == AutoProxyMode.LATCH) {
                    systemServerSettings.autoProxy = null
                    database.updateSystemServerSettings(systemServerSettings)
                } else if (systemServerSettings.autoProxyMode == AutoProxyMode.FALLBACK && system.autoType == AutoProxyMode.LATCH) {
                    system.autoProxy = null
                    database.updateSystem(system)
                }
            }
        } else {
            // Allows AutoProxy to be disabled at a server level.
            if (systemServerSettings.autoProxyMode == AutoProxyMode.OFF) return
            val memberId = if (systemServerSettings.autoProxyMode == AutoProxyMode.FALLBACK) system.autoProxy else systemServerSettings.autoProxy
            val member = database.fetchMemberFromSystem(system.id, memberId ?: return) ?: return

            WebhookUtil.prepareMessage(message, system,  member, null).send()
        }
    }
}

suspend fun ReactionAddEvent.onReactionAdd() {
    // TODO: "Fetch the reaction and perform operations"
    // DatabaseMessage should be non-null, else it's meaningless here
    val databaseMessage = database.fetchMessage(messageId) ?: return
    when (emoji.name) {
        "❌", "🗑️" -> {
            // System needs to be non-null.
            val system = database.fetchSystemFromUser(userId.value) ?: return
            if (databaseMessage.systemId == system.id) {
                message.delete("User requested message deletion.")
                databaseMessage.deleted = true
                database.updateMessage(databaseMessage)
            }
        }
        "❗", "🔔" -> {
            // TODO: Add a jump to message embed
            message.channel.createMessage("Psst.. ${databaseMessage.memberName} (<@${databaseMessage.userId}>)$ellipsis You were pinged by <@${userId.value}>")
            getMessage().deleteReaction(userId, emoji)
        }
        "❓", "❔" -> {
            val system = database.fetchSystemFromId(databaseMessage.systemId)
                ?: return

            val member = database.fetchMemberFromSystem(databaseMessage.systemId, databaseMessage.memberId)
                ?: return

            val guild = getGuild()
            val settings = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)

            getUser().getDmChannel().createMessage {
                embed {
                    val systemName = system.name ?: system.id
                    author {
                        name = member.displayName?.let { "$it (${member.name})\u2007•\u2007$systemName" } ?: "${member.name}\u2007•\u2007$systemName"
                        icon = member.avatarUrl
                    }
                    member.avatarUrl?.let {
                        thumbnail {
                            url = it
                        }
                    }
                    color = member.color.kordColor()
                    description = member.description
                    settings?.nickname?.let {
                        field {
                            name = "Server Name"
                            value = "> $it\n*For ${guild?.name}*"
                            inline = true
                        }
                    }
                    member.pronouns?.let {
                        field {
                            name = "Pronouns"
                            value = it
                            inline = true
                        }
                    }
                    member.birthday?.let {
                        field {
                            name = "Birthday"
                            value = it
                            inline = true
                        }
                    }
                    footer {
                        text = "Member ID \u2009• \u2009${member.id}\u2007|\u2007System ID \u2009• \u2009${system.id}\u2007|\u2007Created "
                    }
                    timestamp = system.timestamp.toKtInstant()
                }
            }
            getMessage().deleteReaction(userId, emoji)
        }
    }
}