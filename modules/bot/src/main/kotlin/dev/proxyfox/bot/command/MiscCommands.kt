package dev.proxyfox.bot.command

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.rest.NamedFile
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.string
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.exporter.Exporter
import dev.proxyfox.importer.import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL

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
        registerCommand(literal("export", ::export))
        registerCommand(literal("time", ::time))
        registerCommand(literal("help", ::help))
        registerCommand(literal("explain", ::explain))
        registerCommand(literal("invite", ::invite))
        registerCommand(literal("source", ::source))
        registerCommand(literal("proxy", ::serverProxyEmpty) {
            literal("off", ::serverProxyOff)
            literal("disable", ::serverProxyOff)
            literal("on", ::serverProxyOn)
            literal("enable", ::serverProxyOn)
        })
        val autoproxy: CommandNode = {
            literal("off", ::proxyOff)
            literal("disable", ::proxyOff)
            literal("latch", ::proxyLatch)
            literal("l", ::proxyLatch)
            literal("front", ::proxyFront)
            literal("f", ::proxyFront)
            greedy("member", ::proxyMember)
            greedy("m", ::proxyMember)
        }
        registerCommand(literal("autoproxy", ::proxyEmpty, autoproxy))
        registerCommand(literal("ap", ::proxyEmpty, autoproxy))

        registerCommand(literal("role", ::roleEmpty) {
            literal("clear", ::roleClear)
            greedy("role", ::role)
        })

        val delete: CommandNode = {
            greedy("message", ::deleteMessage)
        }
        registerCommand(literal("delete", ::deleteMessage, delete))
        registerCommand(literal("del", ::deleteMessage, delete))
        registerCommand(literal("d", ::deleteMessage, delete))

        val reproxy: CommandNode = {
            string("message", ::reproxyMessage) {
                greedy("member", ::reproxyMessage)
            }
            greedy("member", ::reproxyMessage)
        }
        registerCommand(literal("reproxy", ::reproxyMessage, reproxy))
        registerCommand(literal("rp", ::reproxyMessage, reproxy))

        val info: CommandNode = {
            greedy("message", ::fetchMessageInfo)
        }
        registerCommand(literal("info", ::fetchMessageInfo, info))
        registerCommand(literal("i", ::fetchMessageInfo, info))

        val ping: CommandNode = {
            greedy("message", ::pingMessageAuthor)
        }
        registerCommand(literal("ping", ::pingMessageAuthor, ping))
        registerCommand(literal("p", ::pingMessageAuthor, ping))
    }

    private suspend fun importEmpty(ctx: MessageHolder): String {
        if (ctx.message.attachments.isEmpty()) return "Please attach a file or link to import"
        val attach = URL(ctx.message.attachments.toList()[0].url)
        val importer = withContext(Dispatchers.IO) {
            import(
                InputStreamReader(attach.openStream()),
                ctx.message.author
            )
        }
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun import(ctx: MessageHolder): String {
        val attach = URL(ctx.params["url"]!![0])
        val importer = withContext(Dispatchers.IO) {
            import(
                InputStreamReader(attach.openStream()),
                ctx.message.author
            )
        }
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun export(ctx: MessageHolder): String {
        database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val export = Exporter.export(ctx.message.author!!.id.value)
        ctx.message.channel.createMessage {
            files.add(NamedFile("export", export.byteInputStream()))
        }
        return ""
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
        database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please provide whether you want autoproxy set to `off`, `latch`, `front`, or a member"
    }

    private suspend fun proxyLatch(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.LATCH
        database.updateSystem(system)
        return "Autoproxy mode is now set to `latch`"
    }

    private suspend fun proxyFront(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.FRONT
        database.updateSystem(system)
        return "Autoproxy mode is now set to `front`"
    }

    private suspend fun proxyMember(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        system.autoType = AutoProxyMode.MEMBER
        system.autoProxy = member.id
        database.updateSystem(system)
        return "Autoproxy mode is now set to ${member.name}"
    }

    private suspend fun proxyOff(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.OFF
        database.updateSystem(system)
        return "Autoproxy disabled"
    }

    private suspend fun serverProxyEmpty(ctx: MessageHolder): String {
        database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please tell me if you want to enable or disable proxy for this server"
    }

    private suspend fun serverProxyOn(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getServerSettingsById(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.LATCH
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been enabled"
    }

    private suspend fun serverProxyOff(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getServerSettingsById(ctx.message.getGuild(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.OFF
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been disabled"
    }

    private suspend fun roleEmpty(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild())
        if (server.proxyRole == 0UL) return "There is no proxy role set."
        return "Current role is <@&${server.proxyRole}>"
    }

    private suspend fun role(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild())
        val roleRaw = ctx.params["role"]!![0]
        val role = roleMatcher.find(roleRaw)?.value?.toULong()
            ?: ctx.message.getGuild().roles.filter { it.name == roleRaw }.firstOrNull()?.id?.value
            ?: return "Please provide a role to set"
        server.proxyRole = role
        database.updateServerSettings(server)
        return "Role updated!"
    }

    private suspend fun roleClear(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild())
        server.proxyRole = 0UL
        database.updateServerSettings(server)
        return "Role removed!"
    }

    private suspend fun getMessageFromContext(system: SystemRecord, ctx: MessageHolder): Message? {
        val messageIdString = ctx.params["message"]?.get(0)
        val messageSnowflake: Snowflake? = messageIdString?.let { Snowflake(it) }
        val channelId = ctx.message.channelId
        val channel = ctx.message.channel.fetchChannelOrNull()
        val databaseMessage = database.fetchLatestMessage(system.id, channelId)
        return if (messageSnowflake != null)
            channel?.getMessage(messageSnowflake)
        else ctx.message.referencedMessage
            ?: databaseMessage?.let { channel?.getMessage(Snowflake(it.newMessageId)) }
    }

    private suspend fun deleteMessage(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val message = getMessageFromContext(system, ctx)
            ?: return "Unable to find message to delete."
        try {
            message.delete("User requested message deletion.")
        } catch (t: Throwable) {
            // TODO: Get the proper exception type and provide a more detailed message
            return "Failed to delete message."
        }
        return ""
    }

    private suspend fun reproxyMessage(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun fetchMessageInfo(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun pingMessageAuthor(ctx: MessageHolder): String {
        TODO()
    }
}