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
import dev.kord.core.entity.Message
import dev.kord.rest.NamedFile
import dev.proxyfox.bot.kordColor
import dev.proxyfox.bot.startTime
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.string
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.toKtInstant
import dev.proxyfox.bot.toShard
import dev.proxyfox.bot.webhook.WebhookUtil
import dev.proxyfox.common.DebugException
import dev.proxyfox.common.ellipsis
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.exporter.Exporter
import dev.proxyfox.importer.import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.net.URL
import kotlin.time.Duration

/**
 * Miscellaneous commands
 * @author Oliver
 * */
object MiscCommands {
    private val roleMatcher = Regex("\\d+")

    suspend fun register() {
        printStep("Registering misc commands", 2)
        registerCommand(literal("import", ::importEmpty) {
            greedy("url", ::import)
        })
        //TODO: export --full
        registerCommand(literal("export", ::export))
        registerCommand(literal("time", ::time))
        registerCommand(literal("help", ::help))
        registerCommand(literal("explain", ::explain))
        registerCommand(literal("invite", ::invite))
        registerCommand(literal("source", ::source))
        registerCommand(literal(arrayOf("proxy", "p"), ::serverProxyEmpty) {
            literal(arrayOf("off", "disable"), ::serverProxyOff)
            literal(arrayOf("on", "enable"), ::serverProxyOn)
        })
        registerCommand(literal(arrayOf("autoproxy", "ap"), ::proxyEmpty) {
            literal(arrayOf("off", "disable"), ::proxyOff)
            literal(arrayOf("latch", "l"), ::proxyLatch)
            literal(arrayOf("front", "f"), ::proxyFront)
            greedy("member", MiscCommands::proxyMember)
        })

        registerCommand(literal("role", ::roleEmpty) {
            literal("clear", ::roleClear)
            greedy("role", ::role)
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

        registerCommand(literal("debug", ::debug))
    }

    private suspend fun debug(ctx: MessageHolder): String {
        val shardid = ctx.message.getGuildOrNull()?.id?.value?.toShard() ?: -1
        ctx.respond {
            title = "ProxyFox Debug"
            var gatewayPing = Duration.ZERO
            if (shardid != -1) {
                gatewayPing = ctx.message.kord.gateway.gateways[shardid]!!.ping.value!!

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
        }

        throw DebugException()
    }

    private suspend fun importEmpty(ctx: MessageHolder): String {
        if (ctx.message.attachments.isEmpty()) return "Please attach a file or link to import"
        val attach = URL(ctx.message.attachments.toList()[0].url)
        val importer = withContext(Dispatchers.IO) {
            attach.openStream().reader().use { import(it, ctx.message.author) }
        }
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun import(ctx: MessageHolder): String {
        val attach = URL(ctx.params["url"]!![0])
        val importer = withContext(Dispatchers.IO) {
            attach.openStream().reader().use { import(it, ctx.message.author) }
        }
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun export(ctx: MessageHolder): String {
        database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val export = Exporter.export(ctx.message.author!!.id.value)
        ctx.sendFiles(NamedFile("system.json", export.byteInputStream()))
        return "Check your DMs~"
    }

    private fun time(ctx: MessageHolder): String {
        val date = System.currentTimeMillis() / 1000
        return "It is currently <t:$date:f>"
    }

    private fun help(ctx: MessageHolder): String =
        """To view commands for ProxyFox, visit <https://github.com/ProxyFox-developers/ProxyFox/blob/master/commands.md>
For quick setup:
- pf>system new name
- pf>member new John Doe
- pf>member "John Doe" proxy j:text"""

    private fun explain(ctx: MessageHolder): String =
        """ProxyFox is modern Discord bot designed to help systems communicate.
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use. Someone will likely be willing to explain further if need be."""

    private fun invite(ctx: MessageHolder): String =
        """Use <https://discord.com/api/oauth2/authorize?client_id=${ctx.message.kord.selfId}&permissions=277696539728&scope=applications.commands+bot> to invite ProxyFox to your server!
To get support, head on over to https://discord.gg/q3yF8ay9V7"""

    private fun source(ctx: MessageHolder): String =
        "Source code for ProxyFox is available at https://github.com/ProxyFox-developers/ProxyFox!"

    private suspend fun proxyEmpty(ctx: MessageHolder): String {
        database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please provide whether you want autoproxy set to `off`, `latch`, `front`, or a member"
    }

    private suspend fun proxyLatch(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.LATCH
        database.updateSystem(system)
        return "Autoproxy mode is now set to `latch`"
    }

    private suspend fun proxyFront(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.FRONT
        database.updateSystem(system)
        return "Autoproxy mode is now set to `front`"
    }

    private suspend fun proxyMember(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        system.autoType = AutoProxyMode.MEMBER
        system.autoProxy = member.id
        database.updateSystem(system)
        return "Autoproxy mode is now set to ${member.name}"
    }

    private suspend fun proxyOff(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.OFF
        database.updateSystem(system)
        return "Autoproxy disabled"
    }

    private suspend fun serverProxyEmpty(ctx: MessageHolder): String {
        database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please tell me if you want to enable or disable proxy for this server"
    }

    private suspend fun serverProxyOn(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.LATCH
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been enabled"
    }

    private suspend fun serverProxyOff(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getOrCreateServerSettingsFromSystem(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.OFF
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been disabled"
    }

    private suspend fun roleEmpty(ctx: MessageHolder): String {
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        if (server.proxyRole == 0UL) return "There is no proxy role set."
        return "Current role is <@&${server.proxyRole}>"
    }

    private suspend fun role(ctx: MessageHolder): String {
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
        val server = database.getOrCreateServerSettings(ctx.message.getGuild())
        server.proxyRole = 0UL
        database.updateServerSettings(server)
        return "Role removed!"
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
            message = m?.newMessageId?.let { Snowflake(it) }?.let { channel?.getMessage(it) }
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
        val memberId = ctx.params["member"]?.get(0)!!
        val member = database.fetchMemberFromSystemAndName(system.id, memberId)
            ?: database.fetchMemberFromSystem(system.id, memberId)
        if (member == null) {
            ctx.respond("Couldn't find member to proxy as", true)
            return ""
        }

        WebhookUtil.prepareMessage(message, system, member, null).send(true)

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
                    value = it
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
        webhook.edit(message.id, databaseMessage.threadId?.let(::Snowflake)) {
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
        if (ctx.message.getAuthorAsMember()?.getPermissions()?.contains(Permission.ManageChannels) != true) return "You do not have the proper permissions to run this command"
        val channel = ctx.params["channel"]?.get(0)
            ?: ctx.message.channelId.value.toString()
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length-1).toULongOrNull()
            ?: return "Provided string is not a valid channel"
        val channelSettings = database.getOrCreateChannel(ctx.message.getGuild().id.value, channelId)
        if (channelSettings.proxyEnabled) return "Proxying is already enabled for <#$channelId>"
        channelSettings.proxyEnabled = true
        database.updateChannel(channelSettings)
        return "Proxying is now enabled for <#$channelId>"
    }

    private suspend fun channelProxyDisable(ctx: MessageHolder): String {
        if (ctx.message.getAuthorAsMember()?.getPermissions()?.contains(Permission.ManageChannels) != true) return "You do not have the proper permissions to run this command"
        val channel = ctx.params["channel"]?.get(0)
            ?: ctx.message.channelId.value.toString()
        val channelId = channel.toULongOrNull()
            ?: channel.substring(2, channel.length-1).toULongOrNull()
            ?: return "Provided string is not a valid channel"
        val channelSettings = database.getOrCreateChannel(ctx.message.getGuild().id.value, channelId)
        if (!channelSettings.proxyEnabled) return "Proxying is already disabled for <#$channelId>"
        channelSettings.proxyEnabled = false
        database.updateChannel(channelSettings)
        return "Proxying is now disabled for <#$channelId>"
    }
}