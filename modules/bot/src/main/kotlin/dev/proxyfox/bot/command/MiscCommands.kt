/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.interaction.*
import dev.proxyfox.bot.*
import dev.proxyfox.bot.command.context.*
import dev.proxyfox.bot.command.node.attachment
import dev.proxyfox.bot.prompts.Button
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.bot.webhook.GuildMessage
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.command.node.builtin.*
import dev.proxyfox.common.*
import dev.proxyfox.database.database
import dev.proxyfox.database.displayDate
import dev.proxyfox.database.etc.exporter.Exporter
import dev.proxyfox.database.etc.importer.ImporterException
import dev.proxyfox.database.etc.importer.import
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.ServerSettingsRecord
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaLocalDate
import java.net.URL
import kotlin.math.floor

/**
 * Miscellaneous commands
 * @author Oliver
 * */
object MiscCommands {
    private val roleMatcher = Regex("\\d+")
    var infoInteractionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()
    var moderationInteractionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()
    var miscInteractionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()

    fun SubCommandBuilder.runs(rootName: String, action: suspend InteractionCommandContext.() -> Boolean) {
        when (rootName) {
            "info" -> infoInteractionExecutors
            "moderation" -> moderationInteractionExecutors
            "misc" -> miscInteractionExecutors
            else -> return
        }[name] = action
    }

    suspend fun Kord.registerMiscCommands() {
        printStep("Registering misc commands", 3)
        deferChatInputCommand("info", "Fetches info about the bot") {
            subCommand("debug", "Fetch debug information about the bot") {
                runs("info") {
                    debug(this)
                }
            }
            subCommand("help", "Get help information") {
                runs("info") {
                    respondSuccess(help)
                    true
                }
            }
            subCommand("about", "Get about information") {
                runs("info") {
                    respondSuccess(explain)
                    true
                }
            }
            subCommand("source", "Get the source code") {
                runs("info") {
                    respondSuccess(source)
                    true
                }
            }
            subCommand("invite", "Get the bot invite and the support server invite") {
                runs("info") {
                    respondSuccess(invite)
                    true
                }
            }
        }
        deferChatInputCommand("moderation", "Moderator-only commands") {
            subCommand("role", "Access the role required for proxying") {
                role("role", "The role required for proxying") {
                    required = false
                }
                clear()
                runs("moderation") {
                    val role = value.interaction.command.roles["role"]
                    val clear = value.interaction.command.booleans["clear"] ?: false
                    role(this, role?.id?.value?.toString(), clear)
                }
            }
            subCommand("mod-delay", "The amount of time to delay proxying for moderation bots") {
                name("delay")
                runs("moderation") {
                    val delay = value.interaction.command.strings["delay"]
                    val guild = getGuild() ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    delay(this, database.getOrCreateServerSettings(guild), delay)
                }
            }
            subCommand("channel-proxy", "Toggle proxying for a specific channel") {
                channel("channel", "The channel to set") {
                    required = true
                }
                bool("value", "The value to set")
                runs("moderation") {
                    val channel = value.interaction.command.channels["channel"]!!.id.value.toString()
                    val enabled = value.interaction.command.booleans["value"]
                    channelProxy(this, channel, enabled)
                }
            }
            subCommand("force-tag", "Toggle the enforcement of a system tag for this server") {
                bool("value", "The value to set")
                runs("moderation") {
                    val enabled = value.interaction.command.booleans["value"]
                    forceTag(this, enabled)
                }
            }
        }
        deferChatInputCommand("misc", "Other commands that don't fit in a category") {
            subCommand("fox", "Gets a random fox picture") {
                runs("misc") {
                    getFox(this)
                }
            }
            subCommand("import", "Import a system") {
                attachment("import", "The file to import") {
                    required = true
                }
                runs("misc") {
                    import(this, URL(value.interaction.command.attachments["import"]!!.url))
                }
            }
            subCommand("export", "Export your system") {
                runs("misc") {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    export(this)
                }
            }
            subCommand("time", "Displays the current time") {
                runs("misc") {
                    time(this)
                }
            }
            subCommand("autoproxy", "Changes the autoproxy type") {
                name("value")
                runs("misc") {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val type: AutoProxyMode? = when(value.interaction.command.strings["value"]) {
                        null -> null
                        "off", "disable" -> AutoProxyMode.OFF
                        "latch", "l" -> AutoProxyMode.LATCH
                        "front", "f" -> AutoProxyMode.FRONT
                        else -> AutoProxyMode.MEMBER
                    }
                    val member = if (type == AutoProxyMode.MEMBER) {
                        val mem = database.findMember(system!!.id, value.interaction.command.strings["member"]!!)
                        if (!checkMember(this, mem)) return@runs false
                        mem
                    } else null
                    proxy(this, system!!, type, member)
                }
            }
            subCommand("proxy", "Toggles proxying for this server") {
                bool("value", "the value to set")
                guild()
                runs("misc") {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val enabled = value.interaction.command.booleans["value"]
                    val guildId = value.interaction.command.integers["server"]?.toULong()?.let { Snowflake(it) } ?: getGuild()?.id
                    guildId ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    val guild = kord.getGuildOrNull(guildId) ?: run {
                        respondFailure("Cannot find server. Am I in it?")
                        return@runs false
                    }
                    val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                    serverProxy(this, serverSystem, enabled)
                }
            }
            subCommand("serverautoproxy", "Changes the autoproxy type for the server") {
                name("value")
                guild()
                runs("misc") {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val type: AutoProxyMode? = when(value.interaction.command.strings["value"]) {
                        null -> null
                        "off", "disable" -> AutoProxyMode.OFF
                        "latch", "l" -> AutoProxyMode.LATCH
                        "front", "f" -> AutoProxyMode.FRONT
                        else -> AutoProxyMode.MEMBER
                    }
                    val member = if (type == AutoProxyMode.MEMBER) {
                        val mem = database.findMember(system!!.id, value.interaction.command.strings["member"]!!)
                        if (!checkMember(this, mem)) return@runs false
                        mem
                    } else null
                    val guildId = value.interaction.command.integers["server"]?.toULong()?.let { Snowflake(it) } ?: getGuild()?.id
                    guildId ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    val guild = kord.getGuildOrNull(guildId) ?: run {
                        respondFailure("Cannot find server. Am I in it?")
                        return@runs false
                    }
                    val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                    serverAutoProxy(this, serverSystem, type, member)
                }
            }
            subCommand("edit", "Edit a message proxied by ProxyFox") {
                string("content", "The content to replace with") {
                    required = true
                }
                int("message", "The message ID to edit") {
                    required = false
                }
                runs("misc") {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    val message = value.interaction.command.integers["message"]?.toULong()?.let { Snowflake(it) }
                    editMessage(this, system!!, message, value.interaction.command.strings["content"]!!)
                }
            }
        }
    }

    suspend fun register() {
        printStep("Registering misc commands", 3)
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
                export(this)
            }
        }
        Commands.parser.literal("time") {
            runs(::time)
        }
        Commands.parser.literal("help") {
            responds(help)
        }
        Commands.parser.literal("explain") {
            responds(explain)
        }
        Commands.parser.literal("invite") {
            responds(invite)
        }
        Commands.parser.literal("source") {
            responds(source)
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
                    val guild = kord.getGuildOrNull(guildId) ?: run {
                        respondFailure("Cannot find server. Am I in it?")
                        return@runs false
                    }
                    val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                    serverProxy(this, serverSystem, null)
                }
                literal("on", "enable") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverProxy(this, serverSystem, true)
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
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverProxy(this, serverSystem, false)
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
        Commands.parser.literal("serverautoproxy", "sap") {
            guild { getGuildId ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val guildId = getGuildId() ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    val guild = kord.getGuildOrNull(guildId) ?: run {
                        respondFailure("Cannot find server. Am I in it?")
                        return@runs false
                    }
                    val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                    serverAutoProxy(this, serverSystem, null, null)
                }
                literal("off", "disable", "o") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverAutoProxy(this, serverSystem, AutoProxyMode.OFF, null)
                    }
                }
                literal("on", "enable", "fallback", "fb") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverAutoProxy(this, serverSystem, AutoProxyMode.FALLBACK, null)
                    }
                }
                literal("latch", "l") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverAutoProxy(this, serverSystem, AutoProxyMode.LATCH, null)
                    }
                }
                literal("front", "f") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system!!.id)
                        serverAutoProxy(this, serverSystem, AutoProxyMode.FRONT, null)
                    }
                }
                greedy("member") { getMem ->
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val member = database.findMember(system!!.id, getMem())
                        if (!checkMember(this, member)) return@runs false
                        val guildId = getGuildId() ?: run {
                            respondFailure("Command not ran in server.")
                            return@runs false
                        }
                        val guild = kord.getGuildOrNull(guildId) ?: run {
                            respondFailure("Cannot find server. Am I in it?")
                            return@runs false
                        }
                        val serverSystem = database.getOrCreateServerSettingsFromSystem(guild, system.id)
                        serverAutoProxy(this, serverSystem, AutoProxyMode.MEMBER, member)
                    }
                }
            }
        }
        Commands.parser.literal("role") {
            runs {
                role(this, null, false)
            }
            unixLiteral("clear", "remove") {
                runs {
                    role(this, null, true)
                }
            }
            greedy("role") { getRole ->
                runs {
                    role(this, getRole(), false)
                }
            }
        }

        Commands.parser.literal("moddelay") {
            runs {
                val guild = getGuild() ?: run {
                    respondFailure("Command not ran in server.")
                    return@runs false
                }
                delay(this, database.getOrCreateServerSettings(guild), null)
            }
            greedy("delay") { getDelay ->
                runs {
                    val guild = getGuild() ?: run {
                        respondFailure("Command not ran in server.")
                        return@runs false
                    }
                    delay(this, database.getOrCreateServerSettings(guild), getDelay())
                }
            }
        }

        Commands.parser.literal("forcetag", "requiretag") {
            runs {
                forceTag(this, null)
            }
            literal("on", "true", "enable", "1") {
                runs {
                    forceTag(this, true)
                }
            }
            literal("off", "false", "disable", "0") {
                runs {
                    forceTag(this, false)
                }
            }
        }

        Commands.parser.literal("delete", "del") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system, true)) return@runs false
                deleteMessage(this, system!!, null)
            }
            int("message") { getMessage ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    deleteMessage(this, system!!, Snowflake(getMessage()))
                }
            }
        }

        Commands.parser.literal("reproxy", "rp") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system, true)) return@runs false
                reproxyMessage(this, system!!, null, null)
            }
            int("message") { getMessage ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    reproxyMessage(this, system!!, Snowflake(getMessage()), null)
                }
                greedy("member") { getMem ->
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system, true)) return@runs false
                        val member = database.findMember(system!!.id, getMem())
                        if (!checkMember(this, member, true)) return@runs false
                        reproxyMessage(this, system, Snowflake(getMessage()), member!!)
                    }
                }
            }
            greedy("member") { getMem ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    val member = database.findMember(system!!.id, getMem())
                    if (!checkMember(this, member, true)) return@runs false
                    reproxyMessage(this, system, null, member!!)
                }
            }
        }

        Commands.parser.literal("info", "i") {
            runs {
                fetchMessageInfo(this, null)
            }
            int("message") { getMessage ->
                runs {
                    fetchMessageInfo(this, Snowflake(getMessage()))
                }
            }
        }

        Commands.parser.literal("ping", "p") {
            runs {
                pingMessageAuthor(this, null)
            }
            int("message") { getMessage ->
                runs {
                    pingMessageAuthor(this, Snowflake(getMessage()))
                }
            }
        }

        Commands.parser.literal("edit", "e") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system, true)) return@runs false
                editMessage(this, system!!, null, null)
            }
            int("message") { getMessage ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    editMessage(this, system!!, Snowflake(getMessage()), null)
                }

                greedy("content") { getContent ->
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system, true)) return@runs false
                        editMessage(this, system!!, Snowflake(getMessage()), getContent())
                    }
                }
            }
            greedy("content") { getContent ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system, true)) return@runs false
                    editMessage(this, system!!, null, getContent())
                }
            }
        }

        Commands.parser.literal("channel", "c") {
            responds("Please provide a channel subcommand")
            literal("proxy", "p") {
                runs {
                    channelProxy(this, null, null)
                }
                string("channel") { getChannelMention ->
                    runs {
                        channelProxy(this, getChannelMention(), null)
                    }
                    literal("on", "true", "enable", "1") {
                        runs {
                            channelProxy(this, getChannelMention(), true)
                        }
                    }
                    literal("off", "false", "disable", "0") {
                        runs {
                            channelProxy(this, getChannelMention(), false)
                        }
                    }
                }
            }
        }

        Commands.parser.literal("debug") {
            runs(::debug)
        }

        Commands.parser.literal("fox") {
            runs(::getFox)
        }

        Commands.parser.literal("token", "t") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system)) return@runs false
                token(this, system!!)
            }
        }

        Commands.parser.literal("trust") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system)) return@runs false
                respondFailure("Please provide a user to perform this action on")
                false
            }
            int("user") { getId ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    trust(this, system!!, getId(), null)
                }
                literal("none", "remove", "clear") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        trust(this, system!!, getId(), TrustLevel.NONE)
                    }
                }
                literal("access", "see", "view") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        trust(this, system!!, getId(), TrustLevel.ACCESS)
                    }
                }
                literal("member", "m") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        trust(this, system!!, getId(), TrustLevel.MEMBER)
                    }
                }
                literal("switch", "sw") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        trust(this, system!!, getId(), TrustLevel.SWITCH)
                    }
                }
                literal("full", "all", "everything") {
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        trust(this, system!!, getId(), TrustLevel.FULL)
                    }
                }
            }
        }
    }

    private suspend fun <T> forceTag(ctx: DiscordContext<T>, enabled: Boolean?): Boolean {
        val server = database.getOrCreateServerSettings(ctx.getGuild() ?: run {
            ctx.respondFailure("You are not in a server.")
            return false
        })

        enabled ?: let {
            ctx.respondPlain("System tag force is currently ${if (server.enforceTag) "enabled" else "disabled"} for this server.")
            return true
        }

        if (!ctx.hasRequired(Permission.ManageGuild)) {
            ctx.respondFailure("You do not have the proper permissions to run this command.")
            return false
        }

        server.enforceTag = enabled
        database.updateServerSettings(server)

        ctx.respondPlain("System tag force is now ${if (server.enforceTag) "enabled" else "disabled"} for this server.")
        return true
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

    private suspend fun <T> trust(
        ctx: DiscordContext<T>,
        system: SystemRecord,
        user: ULong,
        trustLevel: TrustLevel?
    ): Boolean {
        trustLevel ?: run {
            val trust = system.trust[user] ?: TrustLevel.NONE
            ctx.respondPlain("User's trust level is currently `${trust.name}`")
            return true
        }

        TimedYesNoPrompt.build(
            runner = ctx.getUser()!!.id,
            channel = ctx.getChannel(),
            message = "Are you sure you want to trust this user with level `${trustLevel.name}`?\nThis can be changed at any time.",
            yes = Button("Trust user", Button.check, ButtonStyle.Primary) {
                system.trust[user] = trustLevel
                database.updateSystem(system)
                content = "User trust updated."
            }
        )

        return true
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun <T> token(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        ctx.respondWarning("Not yet implemented")
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

    private suspend fun <T> export(ctx: DiscordContext<T>): Boolean {
        val export = Exporter.export(ctx.getUser()!!.id.value)
        val message = ctx.respondFiles(
            null,
            NamedFile("system.json", ChannelProvider { export.byteInputStream().toByteReadChannel() })
        )
        message.channel.createMessage(message.attachments.elementAt(0).url)
        ctx.respondSuccess("Check your DMs~")
        return true
    }

    private suspend fun <T> time(ctx: DiscordContext<T>): Boolean {
        val date = System.currentTimeMillis() / 1000
        ctx.respondSuccess("It is currently <t:$date:f>")
        return true
    }

    // TODO: Provide better help
    private const val help: String =
        """To view commands for ProxyFox, visit <https://github.com/The-ProxyFox-Group/ProxyFox/blob/master/commands.md>
For quick setup:
- pf>system new name
- pf>member new John Doe
- pf>member "John Doe" proxy j:text"""

    private const val explain: String =
        """ProxyFox is modern Discord bot designed to help systems communicate.
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use. Someone will likely be willing to explain further if need be."""

    private val invite: String =
        """Use <https://discord.com/api/oauth2/authorize?client_id=${kord.selfId}&permissions=277696539728&scope=applications.commands+bot> to invite ProxyFox to your server!
To get support, head on over to https://discord.gg/q3yF8ay9V7"""

    private const val source: String =
        "Source code for ProxyFox is available at https://github.com/The-ProxyFox-Group/ProxyFox!"

    private suspend fun <T> proxy(ctx: DiscordContext<T>, system: SystemRecord, mode: AutoProxyMode?, member: MemberRecord?): Boolean {
        mode ?: run {
            val currMember = system.autoProxy?.let { database.fetchMemberFromSystem(system.id, it) }
            ctx.respondSuccess("Autoproxy is set to ${currMember?.showDisplayName() ?: system.autoType.name}")
            return true
        }

        system.autoType = mode
        val response = if (member != null) {system.autoProxy = member.id; "Now autoproxying as ${member.showDisplayName()}"} else "Autoproxy mode is now set to ${mode.name}"
        database.updateSystem(system)
        ctx.respondSuccess(response)
        return true
    }

    private suspend fun <T> serverAutoProxy(ctx: DiscordContext<T>, systemServer: SystemServerSettingsRecord, mode: AutoProxyMode?, member: MemberRecord?): Boolean {
        mode ?: run {
            val currMember = systemServer.autoProxy?.let { database.fetchMemberFromSystem(systemServer.systemId, it) }
            ctx.respondSuccess("Autoproxy is set to ${currMember?.showDisplayName() ?: systemServer.autoProxyMode.name}")
            return true
        }

        systemServer.autoProxyMode = mode
        val response = if (member != null) {systemServer.autoProxy = member.id; "Now autoproxying as ${member.showDisplayName()}"} else "Autoproxy mode is now set to ${mode.name}"
        database.updateSystemServerSettings(systemServer)
        ctx.respondSuccess(response)
        return true
    }

    private suspend fun <T> serverProxy(ctx: DiscordContext<T>, systemServer: SystemServerSettingsRecord, enabled: Boolean?): Boolean {
        enabled ?: run {
            ctx.respondSuccess("Proxy for this server is currently ${if (systemServer.proxyEnabled) "enabled" else "disabled"}.")
            return false
        }

        systemServer.proxyEnabled = enabled
        database.updateSystemServerSettings(systemServer)
        ctx.respondSuccess("Proxy for this server has been ${if (enabled) "enabled" else "disabled"}")
        return true
    }

    private suspend fun <T> role(ctx: DiscordContext<T>, roleRaw: String?, clear: Boolean): Boolean {
        val server = database.getOrCreateServerSettings(ctx.getGuild() ?: run {
            ctx.respondFailure("You are not in a server.")
            return false
        })
        if (!ctx.hasRequired(Permission.ManageGuild)) {
            ctx.respondFailure("You do not have the proper permissions to run this command.")
            return false
        }
        if (clear) {
            server.proxyRole = 0UL
            database.updateServerSettings(server)
            ctx.respondSuccess("Role cleared!")
        }
        roleRaw ?: run {
            if (server.proxyRole == 0UL) {
                ctx.respondFailure("There is no proxy role set.")
                return false
            }
            ctx.respondSuccess("Current role is <@&${server.proxyRole}>")
            return true
        }
        val role = roleMatcher.find(roleRaw)?.value?.toULong()
            ?: ctx.getGuild()!!.roles.filter { it.name == roleRaw }.firstOrNull()?.id?.value
            ?: run {
                ctx.respondFailure("Please provide a role to set.")
                return false
            }
        server.proxyRole = role
        database.updateServerSettings(server)
        ctx.respondSuccess("Role updated!")
        return true
    }

    private suspend fun <T> delay(ctx: DiscordContext<T>, server: ServerSettingsRecord, delayStr: String?): Boolean {
        if (!ctx.hasRequired(Permission.ManageGuild)) {
            ctx.respondFailure("You do not have the proper permissions to run this command")
            return false
        }
        delayStr ?: run {
            if (server.moderationDelay <= 0) {
                ctx.respondFailure("There is no moderation delay present.")
                return false
            }
            ctx.respondSuccess("Current moderation delay is ${server.moderationDelay}ms")
            return true
        }
        val delay = delayStr.parseDuration()
        delay.right?.let {
            ctx.respondFailure(it)
            return false
        }
        var millis = delay.left!!.inWholeMilliseconds
        if (millis > 30000L) {
            millis = 30000L
        }
        server.moderationDelay = millis.toShort()
        database.updateServerSettings(server)
        ctx.respondSuccess("Moderation delay set to ${millis}ms!")
        return true
    }

    private suspend fun <T> deleteMessage(ctx: DiscordContext<T>, system: SystemRecord, message: Snowflake?): Boolean {
        val messages = ctx.getDatabaseMessage(system, message)
        val discordMessage = messages.first
        discordMessage ?: run {
            ctx.respondFailure("Unable to find message to delete.", true)
            return false
        }
        val databaseMessage = messages.second
        databaseMessage ?: run {
            ctx.respondFailure("This message is either too old or wasn't proxied by ProxyFox", true)
            return false
        }
        if (databaseMessage.systemId != system.id) {
            ctx.respondFailure("You weren't the original creator of this message.", true)
            return false
        }
        discordMessage.delete("User requested message deletion.")
        ctx.tryDeleteTrigger("User requested message deletion")
        databaseMessage.deleted = true
        database.updateMessage(databaseMessage)
        ctx.optionalSuccess("Message deleted.")
        return true
    }

    private suspend fun <T> reproxyMessage(ctx: DiscordContext<T>, system: SystemRecord, message: Snowflake?, member: MemberRecord?): Boolean {
        member ?: run {
            ctx.respondFailure("Please provide the member to reproxy as.")
            return false
        }

        val messages = ctx.getDatabaseMessage(system, message)
        val discordMessage = messages.first
        discordMessage ?: run {
            ctx.respondFailure("Unable to find message to delete.", true)
            return false
        }
        val databaseMessage = messages.second
        databaseMessage ?: run {
            ctx.respondFailure("This message is either too old or wasn't proxied by ProxyFox", true)
            return false
        }
        if (databaseMessage.systemId != system.id) {
            ctx.respondFailure("You weren't the original creator of this message.", true)
            return false
        }

        val server = database.getOrCreateServerSettings(discordMessage.getGuild())

        val serverSystem = database.getOrCreateServerSettingsFromSystem(databaseMessage.guildId, system.id)

        if (serverSystem.autoProxyMode == AutoProxyMode.LATCH) {
            serverSystem.autoProxy = member.id
            database.updateSystemServerSettings(serverSystem)
        } else if (serverSystem.autoProxyMode == AutoProxyMode.FALLBACK && system.autoType == AutoProxyMode.LATCH) {
            system.autoProxy = member.id
            database.updateSystem(system)
        }

        val serverMember = database.fetchMemberServerSettingsFromSystemAndMember(ctx.getGuild(), system.id, member.id)

        val guildMessage =
            GuildMessage(discordMessage, ctx.getGuild()!!, discordMessage.channel.asChannelOf(), ctx.getUser()!!)

        WebhookUtil.prepareMessage(
            guildMessage,
            discordMessage.content,
            system,
            member,
            null,
            serverMember,
            server.moderationDelay.toLong(),
            server.enforceTag
        )?.send(true)
            ?: throw AssertionError("Message could not be reproxied. Is the contents empty?")

        databaseMessage.deleted = true
        database.updateMessage(databaseMessage)
        ctx.tryDeleteTrigger("User requested message deletion")
        ctx.optionalSuccess("Message reproxied.")
        return true
    }

    private suspend fun <T> fetchMessageInfo(ctx: DiscordContext<T>, message: Snowflake?): Boolean {
        val messages = ctx.getDatabaseMessage(null, message)
        val discordMessage = messages.first
        discordMessage ?: run {
            ctx.respondFailure("Unable to find message to delete.", true)
            return false
        }
        val databaseMessage = messages.second
        databaseMessage ?: run {
            ctx.respondFailure("This message is either too old or wasn't proxied by ProxyFox", true)
            return false
        }

        val system = database.fetchSystemFromId(databaseMessage.systemId)
        if (system == null) {
            ctx.respondFailure("Targeted message's system has since been deleted.", true)
            return false
        }

        val member = database.fetchMemberFromSystem(databaseMessage.systemId, databaseMessage.memberId)
        if (member == null) {
            ctx.respondFailure("Targeted message's member has since been deleted.", true)
            return false
        }

        val guild = discordMessage.getGuild()
        val settings = database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)

        ctx.respondEmbed(true) {
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
                    value = it.toJavaLocalDate().displayDate()
                    inline = true
                }
            }
            footer {
                text = "Member ID \u2009• \u2009${member.id}\u2007|\u2007System ID \u2009• \u2009${system.id}\u2007|\u2007Created "
            }
            timestamp = system.timestamp
        }

        ctx.tryDeleteTrigger("User requested message deletion")
        ctx.optionalSuccess("Info fetched.")
        return true
    }

    private suspend fun <T> editMessage(ctx: DiscordContext<T>, system: SystemRecord, message: Snowflake?, content: String?): Boolean {
        val messages = ctx.getDatabaseMessage(system, message)
        val discordMessage = messages.first
        discordMessage ?: run {
            ctx.respondFailure("Unable to find message to edit.", true)
            return false
        }
        val databaseMessage = messages.second
        databaseMessage ?: run {
            ctx.respondFailure("This message is either too old or wasn't proxied by ProxyFox", true)
            return false
        }

        content ?: run {
            ctx.respondFailure(
                "Please provide message content to edit with.\n" +
                        "To delete the message, run `pf>delete`",
                true
            )
            return false
        }
        val channel = ctx.getChannel()
        val webhook = WebhookUtil.createOrFetchWebhookFromCache(channel.fetchChannel())
        webhook.edit(discordMessage.id, if (channel is ThreadChannelBehavior) channel.id else null) {
            this.content = content
        }
        ctx.tryDeleteTrigger("User requested message deletion")
        ctx.optionalSuccess("Edited message")
        return true
    }

    private suspend fun <T> pingMessageAuthor(ctx: DiscordContext<T>, message: Snowflake?): Boolean {
        val messages = ctx.getDatabaseMessage(null, message)
        val discordMessage = messages.first
        if (discordMessage == null) {
            ctx.respondFailure("Targeted message doesn't exist.", true)
            return false
        }
        val databaseMessage = messages.second
        if (databaseMessage == null) {
            ctx.respondFailure("Targeted message is either too old or wasn't proxied by ProxyFox")
            return false
        }
        ctx.tryDeleteTrigger("User requested message deletion")
        // TODO: Add a jump to message embed
        ctx.getChannel().createMessage("Psst.. ${databaseMessage.memberName} (<@${databaseMessage.userId}>)$ellipsis You were pinged by <@${ctx.getUser()!!.id}>")
        ctx.optionalSuccess("Author pinged.")
        return true
    }

    private suspend fun <T> channelProxy(ctx: DiscordContext<T>, channel: String?, value: Boolean?): Boolean {
        if (!ctx.hasRequired(Permission.ManageChannels)) {
            ctx.respondFailure("You do not have the proper permissions to run this command")
            return false
        }
        ctx.getGuild() ?: run {
            ctx.respondFailure("You need to run this command in a server.")
        }
        channel ?: run {
            ctx.respondFailure("Please provide a channel to change")
            return false
        }
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length - 1).toULongOrNull()
            ?: run {
                ctx.respondFailure("Provided string is not a valid channel")
                return false
            }
        val channelSettings = database.getOrCreateChannel(ctx.getChannel().id.value, channelId)
        value ?: run {
            ctx.respondSuccess("Proxying is currently ${if (channelSettings.proxyEnabled) "enabled" else "disabled"} for <#$channelId>.")
            return true
        }
        channelSettings.proxyEnabled = value
        database.updateChannel(channelSettings)
        ctx.respondSuccess("Proxying is now ${if (value) "enabled" else "disabled"} for <#$channelId>")
        return true
    }
}