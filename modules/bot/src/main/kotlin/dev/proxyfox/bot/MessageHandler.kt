/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.kord.common.entity.*
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.builder.components.emoji
import dev.kord.core.cache.data.AttachmentData
import dev.kord.core.cache.data.EmbedData
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Embed
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.bot.string.parser.parseString
import dev.proxyfox.bot.webhook.GuildMessage
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.common.ellipsis
import dev.proxyfox.database.database
import dev.proxyfox.database.displayDate
import dev.proxyfox.database.etc.exporter.Exporter
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import org.slf4j.LoggerFactory

val prefixRegex = Regex("^(?:(<@!?${kord.selfId}>)|pf[>;!:])\\s*", RegexOption.IGNORE_CASE)

private val logger = LoggerFactory.getLogger("MessageHandler")

suspend fun ButtonInteractionCreateEvent.onInteraction() {
    // This should only ever work for export.
    if (interaction.componentId != "export") {
        return
    }

    val ephemeral = interaction.deferEphemeralResponse()

    val system = database.fetchSystemFromUser(interaction.user)

    if (system == null) {
        ephemeral.respond {
            content = "You don't have a system registered."
        }
        return
    }

    val export = Exporter.export(interaction.user.id.value)

    val dm = interaction.user.getDmChannelOrNull()
    if (dm != null) try {
        dm.createMessage {
            content = "Here you go! Note: If you modify your system after this, the daily nag will resume."
            addFile("system.json", export.byteInputStream())
        }
        ephemeral.respond { content = "Check your DMs~" }
        system.exported = true
        database.updateSystem(system)
        return
    } catch (e: Exception) {
        logger.warn("Unable to DM {}, falling back to ephemeral", interaction.user, e)
    }

    ephemeral.respond {
        content = "Here you go! Note: As I failed to DM you, you'll continue to get a daily nag."
        addFile("system.json", export.byteInputStream())
    }
}


suspend fun MessageCreateEvent.onMessageCreate() {
    val user = message.author ?: return
    val channel = message.channel
    val guildChannel = channel.asChannelOfOrNull<GuildChannel>()

    // Return if bot
    if (message.webhookId != null || user.isBot || (message.type != MessageType.Default && message.type != MessageType.Reply)) return

    if (!channel.selfCanSend()) {
        return
    }

    // Get message content to check with regex
    val content = message.content
    val matcher = prefixRegex.toPattern().matcher(content)
    if (matcher.find()) {
        // Remove the prefix to pass into dispatcher
        val contentWithoutRegex = content.substring(matcher.end())

        if (contentWithoutRegex.isBlank() && matcher.start(1) >= 0) {
            channel.createMessage {
                this.content = """Hi, I'm ProxyFox! My prefix is `pf>`.
                    |Unfortunately, I'm shutting down <t:1709316000:R>, on <t:1709316000:F>.
                    |If you have a system registered with me, you can `pf;export` it now. [Click here to learn more](<https://proxyfox.dev>)""".trimMargin()
                this.components.add(ActionRowBuilder().apply {
                    this.interactionButton(ButtonStyle.Secondary, "export") {
                        label = "Export"
                        emoji(ReactionEmoji.Unicode("\uD83D\uDCE4"))
                    }
                    this.linkButton("https://proxyfox.dev") {
                        label = "Read More"
                    }
                })
            }
        } else {
            // Run the command
            val output = parseString(contentWithoutRegex, message) ?: return
            // Send output message if exists
            if (output.isNotBlank())
                channel.createMessage {
                    this.content = "$output\n---\n⚠️ I'm shutting down <t:1709316000:R>, export your system now?"
                    this.components.add(ActionRowBuilder().apply {
                        this.interactionButton(ButtonStyle.Secondary, "export") {
                            label = "Export"
                            emoji(ReactionEmoji.Unicode("\uD83D\uDCE4"))
                        }
                        this.linkButton("https://proxyfox.dev") {
                            label = "Read More"
                        }
                    })
                }
        }
    } else if (guildChannel != null && guildChannel.selfHasPermissions(Permissions(Permission.ManageWebhooks, Permission.ManageMessages))) {
        val guild = guildChannel.getGuild()
        val hasStickers = message.stickers.isNotEmpty()
        // TODO: Boost to upload limit; 8 MiB is default.
        val hasOversizedFiles = message.attachments.fold(0L) { size, attachment -> size + attachment.size } >= UPLOAD_LIMIT
        val isOversizedMessage = content.length > 2000
        if (hasStickers || hasOversizedFiles || isOversizedMessage) {
            logger.trace("Denying proxying {} ({}) in {} ({}) due to Discord bot constraints", user.tag, user.id, guild.name, guild.id)
            return
        }

        handleProxying(GuildMessage(message, guild, channel), isEdit = false)
    }
}

suspend fun MessageUpdateEvent.onMessageUpdate() {
    val guild = kord.getGuild(new.guildId.value ?: return) ?: return
    val content = new.content.value ?: return
    val authorRaw = new.author.value ?: return
    if (authorRaw.bot.discordBoolean) return

    val hasStickers = !new.stickers.value.isNullOrEmpty()
    val hasOversizedFiles = new.attachments.value?.any { it.size >= UPLOAD_LIMIT } ?: false
    val isOversizedMessage = content.length > 2000

    if (hasStickers || hasOversizedFiles || isOversizedMessage) {
        logger.trace(
            "Denying proxying {}#{} ({}) in {} ({}) due to Discord bot constraints",
            authorRaw.username, authorRaw.discriminator, authorRaw.id, guild.name, guild.id
        )
        return
    }

    val author = kord.getUser(authorRaw.id) ?: return

    val guildMessage = GuildMessage(
        messageId,
        content,
        author,
        channel,
        guild,
        new.attachments.value?.map { Attachment(AttachmentData.from(it), kord) } ?: emptySet(),
        new.embeds.value?.map { Embed(EmbedData.from(it), kord) } ?: emptyList(),
        new.messageReference.value?.id?.value?.let { channel.getMessage(it) },
        message,
    )

    handleProxying(
        guildMessage,
        isEdit = true,
    )
}

private suspend fun handleProxying(
    message: GuildMessage,
    isEdit: Boolean,
) {
    val user = message.author
    val channel = message.channel
    val guild = message.guild
    val content = message.content
    val userId: ULong = user.id.value

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
    val proxy = database.fetchProxyTagFromMessage(userId, content)
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

        WebhookUtil.prepareMessage(message, content, system, member, proxy, memberServer, server.moderationDelay.toLong())?.send()
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
    } else if (!isEdit) {
        val member = getAutoProxyMember(system, systemServerSettings) ?: return

        // Respect member settings.
        val memberServer = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)
        if (memberServer?.proxyEnabled == false) return

        WebhookUtil.prepareMessage(message, content, system, member, null, memberServer, server.moderationDelay.toLong())?.send()
    }
}

private suspend fun getAutoProxyMember(system: SystemRecord, server: SystemServerSettingsRecord): MemberRecord? {
    return when (server.autoProxyMode) {
        AutoProxyMode.FALLBACK -> when (system.autoType) {
            AutoProxyMode.FRONT -> database.fetchFrontingMembersFromSystem(system.id)?.firstOrNull().nullIfApDisabled()
            AutoProxyMode.LATCH -> database.fetchMemberFromSystem(system.id, system.autoProxy ?: return null).nullIfApDisabled()
            AutoProxyMode.MEMBER -> database.fetchMemberFromSystem(system.id, system.autoProxy ?: return null)
            AutoProxyMode.FALLBACK, AutoProxyMode.OFF -> null
        }

        AutoProxyMode.FRONT -> database.fetchFrontingMembersFromSystem(system.id)?.firstOrNull().nullIfApDisabled()
        AutoProxyMode.LATCH -> database.fetchMemberFromSystem(system.id, server.autoProxy ?: return null).nullIfApDisabled()
        AutoProxyMode.MEMBER -> database.fetchMemberFromSystem(system.id, server.autoProxy ?: return null)
        AutoProxyMode.OFF -> null
    }
}

private fun MemberRecord?.nullIfApDisabled(): MemberRecord? = if (this != null && autoProxy) this else null

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
            message.deleteReaction(userId, emoji)
        }
        "❓", "❔" -> {
            val system = database.fetchSystemFromId(databaseMessage.systemId)
                ?: return

            val member = database.fetchMemberFromSystem(databaseMessage.systemId, databaseMessage.memberId)
                ?: return

            val guild = getGuild()
            val settings = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)

            val user = kord.getUser(Snowflake(databaseMessage.userId))

            getUser().getDmChannel().createMessage {
                content = "Message by ${member.showDisplayName()} was sent by <@${databaseMessage.userId}> (${user?.tag ?: "Unknown user"})"
                embed {
                    val systemName = system.name ?: system.id
                    author {
                        name = member.displayName?.let { "$it (${member.name})\u2007•\u2007$systemName" } ?: "${member.name}\u2007•\u2007$systemName"
                        icon = member.avatarUrl.httpUri()
                    }
                    member.avatarUrl?.let {
                        thumbnail {
                            url = it.httpUri()
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
                            value = it.displayDate()
                            inline = true
                        }
                    }
                    footer {
                        text = "Member ID \u2009• \u2009${member.id}\u2007|\u2007System ID \u2009• \u2009${system.id}\u2007|\u2007Created "
                    }
                    timestamp = system.timestamp.toKtInstant()
                }
            }
            message.deleteReaction(userId, emoji)
        }
    }
}