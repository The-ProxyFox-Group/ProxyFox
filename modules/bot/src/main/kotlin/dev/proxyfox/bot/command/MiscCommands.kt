/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.rest.NamedFile
import dev.proxyfox.bot.*
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.context.guild
import dev.proxyfox.bot.command.context.runs
import dev.proxyfox.bot.command.node.attachment
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.string
import dev.proxyfox.bot.string.dsl.unixLiteral
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.webhook.GuildMessage
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.command.node.builtin.greedy
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.common.*
import dev.proxyfox.database.database
import dev.proxyfox.database.displayDate
import dev.proxyfox.database.etc.exporter.Exporter
import dev.proxyfox.database.etc.importer.ImporterException
import dev.proxyfox.database.etc.importer.import
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.net.URL
import kotlin.math.floor

/**
 * Miscellaneous commands
 * @author Oliver
 * */
object MiscCommands {
    private val roleMatcher = Regex("\\d+")

    suspend fun Kord.registerMiscCommands() {
        createGlobalChatInputCommand("info", "Fetches info about the bot") {

        }
        createGlobalChatInputCommand("moderation", "Moderator-only commands") {

        }
        createGlobalChatInputCommand("misc", "Other commands that don't fit in a category") {

        }
    }

    suspend fun register() {
        printStep("Registering misc commands", 2)
        Commands.parser.literal("import") {
            runs {
                import(this, null)
            }
            attachment("file") { getFile ->
                runs {
                    import(this, URL(getFile().url))
                }
            }
            greedy("file") { getFile ->
                runs {
                    import(this, URL(getFile()))
                }
            }
        }
        //TODO: export --full
        Commands.parser.literal("export") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system)) return@runs false
                export(this, system!!)
            }
        }
        Commands.parser.literal("time") {
            runs {
                time(this)
            }
        }
        Commands.parser.literal("help") {
            runs {
                respondSuccess(getHelp())
                true
            }
        }
        Commands.parser.literal("explain") {
            runs {
                respondSuccess(getExplain())
                true
            }
        }
        Commands.parser.literal("invite") {
            runs {
                respondSuccess(getInvite())
                true
            }
        }
        Commands.parser.literal("source") {
            runs {
                respondSuccess(getSource())
                true
            }
        }
        Commands.parser.literal("proxy", "p") {
            guild { getGuildId ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val guildId = getGuildId() ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    val guild = kord.getGuild(guildId) ?: run {
                        respondFailure("Cannot find server. Am I in it?")
                        return@runs false
                    }
                    val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                    serverProxy(this, system, serverSystem, null)
                }
                literal("on", "enable") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuild(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverProxy(this, system, serverSystem, true)
                    }
                }
                literal("off", "disable") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuild(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverProxy(this, system, serverSystem, false)
                    }
                }
            }
        }
        Commands.parser.literal("autoproxy", "ap") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system)) return@runs false
                proxy(this, system!!, null, null)
            }
            literal("off", "disable", "o") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    proxy(this, system!!, AutoProxyMode.OFF, null)
                }
            }
            literal("latch", "l") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    proxy(this, system!!, AutoProxyMode.LATCH, null)
                }
            }
            literal("front", "f") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    proxy(this, system!!, AutoProxyMode.FRONT, null)
                }
            }
            greedy("member") { getMem ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val member = database.findMember(system!!.id, getMem())
                    if (!checkMember(this, member)) return@runs false
                    proxy(this, system, AutoProxyMode.MEMBER, member)
                }
            }
        }

        registerCommand(literal(arrayOf("serverautoproxy", "sap"), ::serverAutoProxyEmpty) {
            literal(arrayOf("off", "disable"), ::serverAutoProxyOff)
            literal(arrayOf("latch", "l"), ::serverAutoProxyLatch)
            literal(arrayOf("front", "f"), ::serverAutoProxyFront)
            literal(arrayOf("on", "enable", "fallback", "fb"), ::serverAutoProxyFallback)
            greedy("member", MiscCommands::serverAutoProxyMember)
        })

        registerCommand(literal("role", ::roleEmpty) {
            unixLiteral("clear", ::roleClear)
            greedy("role", ::role)
        })

        registerCommand(literal("moddelay", ::delayEmpty) {
            greedy("delay", ::delay)
        })

        registerCommand(literal(arrayOf("delete", "del"), ::deleteMessage) {
            greedy("message", ::deleteMessage)
        })

        registerCommand(literal(arrayOf("reproxy", "rp"), ::reproxyMessage) {
            greedy("member", ::reproxyMessage)
        })

        registerCommand(literal(arrayOf("info", "i"), ::fetchMessageInfo) {
            greedy("message", MiscCommands::fetchMessageInfo)
        })

        registerCommand(literal(arrayOf("ping", "p"), ::pingMessageAuthor) {
            greedy("message", ::pingMessageAuthor)
        })

        registerCommand(literal(arrayOf("edit", "e"), ::editMessage) {
            greedy("content", ::editMessage)
        })

        registerCommand(literal(arrayOf("channel", "c"), ::channelEmpty) {
            literal(arrayOf("proxy", "p"), ::channelProxy) {
                string("channel", ::channelProxy) {
                    literal(arrayOf("on", "enable"), ::channelProxyEnable)
                    literal(arrayOf("of", "disable"), ::channelProxyDisable)
                }
            }
        })

        Commands.parser.literal("debug") {
            runs {
                debug(this)
            }
        }

        Commands.parser.literal("fox") {
            runs {
                getFox(this)
            }
        }
    }

    private suspend fun <T> getFox(ctx: DiscordContext<T>): Boolean {
        ctx.respondEmbed {
            val fox = FoxFetch.fetch()
            title = "**Link**"
            url = fox
            image = fox
        }
        return true
    }

    private suspend fun <T> debug(ctx: DiscordContext<T>): Boolean {
        val shardid = ctx.getGuild()?.id?.value?.toShard() ?: 0
        ctx.respondEmbed {
            title = "ProxyFox Debug"
            val gatewayPing = kord.gateway.gateways[shardid]!!.ping.value!!
            field {
                inline = true
                name = "Shard ID"
                value = "$shardid"
            }
            field {
                inline = true
                name = "Gateway Ping"
                value = "$gatewayPing"
            }

            val databasePing = database.ping()
            field {
                inline = true
                name = "Database Ping"
                value = "$databasePing"
            }

            val totalPing = gatewayPing + databasePing
            field {
                inline = true
                name = "Total Ping"
                value = "$totalPing"
            }

            field {
                inline = true
                name = "Uptime"
                value = "${(Clock.System.now() - startTime).inWholeHours} hours"
            }

            field {
                inline = true
                name = "Database Implementation"
                value = database.getDatabaseName()
            }

            field {
                inline = true
                name = "Commit Hash"
                value = hash
            }

            field {
                inline = true
                name = "JVM Version"
                value = Runtime.version().toString()
            }

            field {
                inline = true
                name = "Memory Usage"
                value = "${floor(getRamUsagePercentage())}%"
            }

            field {
                inline = true
                name = "Thread Count"
                value = "${getThreadCount()}"
            }
        }

        throw DebugException()
    }

    private suspend fun <T> import(ctx: DiscordContext<T>, url: URL?): Boolean {
        url ?: run {
            ctx.respondFailure("Please provide a file to import")
            return false
        }

        return try {
            val importer = withContext(Dispatchers.IO) {
                url.openStream().reader().use { import(it, ctx.getUser()) }
            }
            ctx.respondSuccess("File imported. created ${importer.createdMembers} member(s), updated ${importer.updatedMembers} member(s)")
            true
        } catch (exception: ImporterException) {
            ctx.respondFailure("Failed to import file: ${exception.message}")
            false
        }
    }

    private suspend fun <T> export(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        val export = Exporter.export(ctx.getUser()!!.id.value)
        ctx.respondFiles(null, NamedFile("system.json", export.byteInputStream()))
        ctx.respondSuccess("Check your DMs~")
        return true
    }

    private suspend fun <T> time(ctx: DiscordContext<T>): Boolean {
        val date = System.currentTimeMillis() / 1000
        ctx.respondSuccess("It is currently <t:$date:f>")
        return true
    }

    private fun getHelp(): String =
        """To view commands for ProxyFox, visit <https://github.com/The-ProxyFox-Group/ProxyFox/blob/master/commands.md>
For quick setup:
- pf>system new name
- pf>member new John Doe
- pf>member "John Doe" proxy j:text"""

    private fun getExplain(): String =
        """ProxyFox is modern Discord bot designed to help systems communicate.
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use. Someone will likely be willing to explain further if need be."""

    private fun getInvite(): String =
        """Use <https://discord.com/api/oauth2/authorize?client_id=${kord.selfId}&permissions=277696539728&scope=applications.commands+bot> to invite ProxyFox to your server!
To get support, head on over to https://discord.gg/q3yF8ay9V7"""

    private fun getSource(): String =
        "Source code for ProxyFox is available at https://github.com/The-ProxyFox-Group/ProxyFox!"

    private suspend fun <T> proxy(ctx: DiscordContext<T>, system: SystemRecord, mode: AutoProxyMode?, member: MemberRecord?): Boolean {
        mode ?: run {
            ctx.respondSuccess("Autoproxy is set to ${system.autoType.name}")
            return true
        }

        system.autoType = mode
        val response = if (member != null) {system.autoProxy = member.id; "Now autoproxying as ${member.showDisplayName()}"} else "Autoproxy mode is now set to ${mode.name}"
        database.updateSystem(system)
        ctx.respondSuccess(response)
        return true
    }

    private suspend fun serverAutoProxyEmpty(ctx: MessageHolder): String {
        database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please provide whether you want autoproxy set to `off`, `latch`, `front`, or a member"
    }

    private suspend fun serverAutoProxyLatch(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.LATCH
        database.updateSystemServerSettings(systemServer)
        return "Autoproxy mode for this server is now set to `latch`"
    }

    private suspend fun serverAutoProxyFront(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.FRONT
        database.updateSystemServerSettings(systemServer)
        return "Autoproxy mode for this server is now set to `front`"
    }

    private suspend fun serverAutoProxyFallback(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.FALLBACK
        database.updateSystemServerSettings(systemServer)
        return "Autoproxy for this server is now using your global settings."
    }

    private suspend fun serverAutoProxyMember(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.MEMBER
        systemServer.autoProxy = member.id
        database.updateSystemServerSettings(systemServer)
        return "Autoproxy mode for this server is now set to ${member.name}"
    }

    private suspend fun serverAutoProxyOff(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.OFF
        database.updateSystemServerSettings(systemServer)
        return "Autoproxy disabled for this server."
    }

    private suspend fun <T> serverProxy(ctx: DiscordContext<T>, system: SystemRecord, systemServer: SystemServerSettingsRecord, enabled: Boolean?): Boolean {
        enabled ?: run {
            ctx.respondSuccess("Proxy for this server is currently ${if (systemServer.proxyEnabled) "enabled" else "disabled"}.")
            return false
        }

        systemServer.proxyEnabled = enabled
        database.updateSystemServerSettings(systemServer)
        ctx.respondSuccess("Proxy for this server has been ${if (enabled) "enabled" else "disabled"}")
        return true
    }

    private suspend fun roleEmpty(ctx: MessageHolder): String {
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        if (server.proxyRole == 0UL) return "There is no proxy role set."
        return "Current role is <@&${server.proxyRole}>"
    }

    private suspend fun role(ctx: MessageHolder): String {
        if (!ctx.hasRequired(Permission.ManageGuild))
            return "You do not have the proper permissions to run this command"
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        val roleRaw = ctx.params["role"]!![0]
        val role = roleMatcher.find(roleRaw)?.value?.toULong()
            ?: ctx.message.getGuild().roles.filter { it.name == roleRaw }.firstOrNull()?.id?.value
            ?: return "Please provide a role to set"
        server.proxyRole = role
        database.updateServerSettings(server)
        return "Role updated!"
    }

    private suspend fun roleClear(ctx: MessageHolder): String {
        if (!ctx.hasRequired(Permission.ManageGuild))
            return "You do not have the proper permissions to run this command"
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        server.proxyRole = 0UL
        database.updateServerSettings(server)
        return "Role removed!"
    }

    private suspend fun delayEmpty(ctx: MessageHolder): String {
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        return if (server.moderationDelay <= 0) {
            "There is no moderation delay present."
        } else {
            "Current moderation delay is ${server.moderationDelay}ms"
        }
    }

    private suspend fun delay(ctx: MessageHolder): String {
        if (!ctx.hasRequired(Permission.ManageGuild))
            return "You do not have the proper permissions to run this command"
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        val delay = ctx.params["delay"]!![0].parseDuration()
        delay.right?.let { return it }
        var millis = delay.left!!.inWholeMilliseconds
        if (millis > 30000L) {
            millis = 30000L
        }
        server.moderationDelay = millis.toShort()
        database.updateServerSettings(server)
        return "Moderation delay set to ${millis}ms"
    }

    private suspend fun getMessageFromContext(system: SystemRecord, ctx: MessageHolder): Pair<Message?, ProxiedMessageRecord?> {
        val messageIdString = ctx.params["message"]?.get(0)
        val messageSnowflake: Snowflake? = messageIdString?.let { Snowflake(it) }
        val channelId = ctx.message.channelId
        val channel = ctx.message.channel.fetchChannelOrNull()
        var message = if (messageSnowflake != null)
            channel?.getMessage(messageSnowflake)
        else ctx.message.referencedMessage

        val databaseMessage = if (message != null)
            database.fetchMessage(message.id)
        else {
            val m = database.fetchLatestMessage(system.id, channelId)
            message = m?.newMessageId?.let { Snowflake(it) }?.let { nullOn404 { channel?.getMessage(it) } }
            m
        }

        return message to databaseMessage
    }
    private suspend fun getSystemlessMessage(ctx: MessageHolder): Pair<Message?, ProxiedMessageRecord?> {
        val messageIdString = ctx.params["message"]?.get(0)
        val messageSnowflake: Snowflake? = messageIdString?.let { Snowflake(it) }
        val channel = ctx.message.channel.fetchChannelOrNull()
        val message = if (messageSnowflake != null)
            channel?.getMessage(messageSnowflake)
        else ctx.message.referencedMessage
        if (message == null) return null to null
        val databaseMessage = database.fetchMessage(message.id)
        return message to databaseMessage
    }

    private suspend fun deleteMessage(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
        if (system == null) {
            ctx.respond("System does not exist. Create one using `pf>system new`", true)
            return ""
        }
        val messages = getMessageFromContext(system, ctx)
        val message = messages.first
        if (message == null) {
            ctx.respond("Unable to find message to delete.", true)
            return ""
        }
        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respond("This message is either too old or wasn't proxied by ProxyFox", true)
            return ""
        }
        if (databaseMessage.systemId != system.id) {
            ctx.respond("You weren't the original creator of this message.", true)
            return ""
        }
        message.delete("User requested message deletion.")
        ctx.message.delete("User requested message deletion")
        databaseMessage.deleted = true
        database.updateMessage(databaseMessage)
        return ""
    }

    private suspend fun reproxyMessage(ctx: MessageHolder): String {
        val guild = ctx.message.getGuildOrNull() ?: return "Run this in a server."
        val system = database.fetchSystemFromUser(ctx.message.author)
        if (system == null) {
            ctx.respond("System does not exist. Create one using `pf>system new`", true)
            return ""
        }
        val messages = getMessageFromContext(system, ctx)
        val message = messages.first
        if (message == null) {
            ctx.respond("Unable to find message to delete.", true)
            return ""
        }
        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respond("Targeted message is either too old or wasn't proxied by ProxyFox", true)
            return ""
        }
        if (databaseMessage.systemId != system.id) {
            ctx.respond("You weren't the original creator of the targeted message.", true)
            return ""
        }
        val member = database.findMember(system.id, ctx.params["member"]?.get(0)!!)
        if (member == null) {
            ctx.respond("Couldn't find member to proxy as", true)
            return ""
        }

        val serverMember = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)

        val guildMessage = GuildMessage(message, guild, message.channel.asChannelOf(), ctx.message.author!!)

        WebhookUtil.prepareMessage(guildMessage, message.content, system, member, null, serverMember)?.send(true)
            ?: throw AssertionError("Message could not be reproxied. Is the contents empty?")

        databaseMessage.deleted = true
        database.updateMessage(databaseMessage)
        ctx.message.delete("User requested message deletion")

        return ""
    }

    private suspend fun fetchMessageInfo(ctx: MessageHolder): String {
        val messages = getSystemlessMessage(ctx)
        val discordMessage = messages.first
        if (discordMessage == null) {
            ctx.respond("Unable to find message to fetch info of", true)
            return ""
        }

        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respond("Targeted message is either too old or wasn't proxied by ProxyFox", true)
            return ""
        }

        val system = database.fetchSystemFromId(databaseMessage.systemId)
        if (system == null) {
            ctx.respond("Targeted message's system has since been deleted.", true)
            return ""
        }

        val member = database.fetchMemberFromSystem(databaseMessage.systemId, databaseMessage.memberId)
        if (member == null) {
            ctx.respond("Targeted message's member has since been deleted.", true)
            return ""
        }

        val guild = discordMessage.getGuild()
        val settings = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)

        ctx.respond(dm=true) {
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
                    value = "> $it\n*For ${guild.name}*"
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

        ctx.message.delete("User requested message deletion")

        return ""
    }

    private suspend fun editMessage(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
        if (system == null) {
            ctx.respond("System does not exist. Create one using `pf>system new`", true)
            return ""
        }
        val messages = getMessageFromContext(system, ctx)
        val message = messages.first
        if (message == null) {
            ctx.respond("Unable to find message to edit.", true)
            return ""
        }
        val channel = message.getChannel()
        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respond("Targeted message is either too old or wasn't proxied by ProxyFox", true)
            return ""
        }
        if (databaseMessage.systemId != system.id) {
            ctx.respond("You weren't the original creator of the targeted message.", true)
            return ""
        }

        val content = ctx.params["content"]?.get(0)
        if (content == null) {
            ctx.respond(
                "Please provide message content to edit with.\n" +
                        "To delete the message, run `pf>delete`",
                true
            )
            return ""
        }

        val webhook = WebhookUtil.createOrFetchWebhookFromCache(channel)
        webhook.edit(message.id, if (channel is ThreadChannelBehavior) channel.id else null) {
            this.content = content
        }
        ctx.message.delete("User requested message deletion")

        return ""
    }

    private suspend fun pingMessageAuthor(ctx: MessageHolder): String {
        val messages = getSystemlessMessage(ctx)
        val discordMessage = messages.first
        if (discordMessage == null) {
            ctx.respond("Targeted message doesn't exist.", true)
            return ""
        }
        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respond("Targeted message is either too old or wasn't proxied by ProxyFox")
            return ""
        }
        ctx.message.delete("User requested message deletion")
        // TODO: Add a jump to message embed
        ctx.respond("Psst.. ${databaseMessage.memberName} (<@${databaseMessage.userId}>)$ellipsis You were pinged by <@${ctx.message.author!!.id}>")
        return ""
    }

    private suspend fun channelEmpty(ctx: MessageHolder): String {
        return "Please provide a channel command"
    }

    private suspend fun channelProxy(ctx: MessageHolder): String {
        val channel = ctx.params["channel"]?.get(0)
            ?: ctx.message.channelId.value.toString()
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length-1).toULongOrNull()
            ?: return "Provided string is not a valid channel"
        val channelSettings = database.getOrCreateChannel(ctx.message.getGuild().id.value, channelId)
        return "Proxying is currently ${if (channelSettings.proxyEnabled) "enabled" else "disabled"} for <#$channelId>."
    }

    private suspend fun channelProxyEnable(ctx: MessageHolder): String {
        if (!ctx.hasRequired(Permission.ManageChannels))
            return "You do not have the proper permissions to run this command"
        val channel = ctx.params["channel"]?.get(0)
            ?: ctx.message.channelId.value.toString()
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length - 1).toULongOrNull()
            ?: return "Provided string is not a valid channel"
        val channelSettings = database.getOrCreateChannel(ctx.message.getGuild().id.value, channelId)
        if (channelSettings.proxyEnabled) return "Proxying is already enabled for <#$channelId>"
        channelSettings.proxyEnabled = true
        database.updateChannel(channelSettings)
        return "Proxying is now enabled for <#$channelId>"
    }

    private suspend fun channelProxyDisable(ctx: MessageHolder): String {
        if (!ctx.hasRequired(Permission.ManageChannels))
            return "You do not have the proper permissions to run this command"
        val channel = ctx.params["channel"]?.get(0)
            ?: ctx.message.channelId.value.toString()
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length - 1).toULongOrNull()
            ?: return "Provided string is not a valid channel"
        val channelSettings = database.getOrCreateChannel(ctx.message.getGuild().id.value, channelId)
        if (!channelSettings.proxyEnabled) return "Proxying is already disabled for <#$channelId>"
        channelSettings.proxyEnabled = false
        database.updateChannel(channelSettings)
        return "Proxying is now disabled for <#$channelId>"
    }
}