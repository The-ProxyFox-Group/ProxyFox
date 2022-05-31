package io.github.proxyfox.command

import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.NamedFile
import io.github.proxyfox.database
import io.github.proxyfox.database.records.misc.AutoProxyMode
import io.github.proxyfox.exporter.Exporter
import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*

/**
 * Miscellaneous commands
 * @author Oliver
 * */
object MiscCommands {
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
    }

    private suspend fun importEmpty(ctx: MessageHolder): String {
        if (ctx.message.attachments.isEmpty()) return "Please attach a file or link to import"
        val attach = URL(ctx.message.attachments.toList()[0].url)
        val importer = io.github.proxyfox.importer.import(
            InputStreamReader(attach.openStream()),
            ctx.message.author!!.id.value.toString()
        )
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun import(ctx: MessageHolder): String {
        val attach = URL(ctx.params["url"]!!)
        val importer = io.github.proxyfox.importer.import(
            InputStreamReader(attach.openStream()),
            ctx.message.author!!.id.value.toString()
        )
        return "File imported. created ${importer.getNewMembers()} member(s), updated ${importer.getUpdatedMembers()} member(s)"
    }

    private suspend fun export(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val export = Exporter.export(ctx.message.author!!.id)
        val exportFile = File.createTempFile("export", "json")
        val exportInputStream = exportFile.inputStream()
        val exportOutputStream = exportFile.outputStream()
        exportOutputStream.bufferedWriter().write(export)
        val message = ctx.message.channel.createMessage {
            files.add(NamedFile("export", exportInputStream))
        }
        return ""
    }

    private suspend fun time(ctx: MessageHolder): String {
        val date = Calendar.getInstance().timeInMillis / 1000
        return "It is currently <t:$date:f>"
    }

    private suspend fun help(ctx: MessageHolder): String =
        """To view commands for ProxyFox, visit https://github.com/ProxyFox-developers/ProxyFox/blob/master/commands.md
For quick setup:
- pf>system new name
- pf>member new John Doe
- pf>member "John Doe" proxy j:text"""

    private suspend fun explain(ctx: MessageHolder): String =
        """ProxyFox is modern Discord bot designed to help systems communicate.
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use. Someone will likely be willing to explain further if need be."""

    private suspend fun invite(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun source(ctx: MessageHolder): String =
        "Source code for ProxyFox is available at https://github.com/ProxyFox-developers/ProxyFox!"

    private suspend fun proxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Please provide whether you want autoproxy set to `off`, `latch`, `front`, or a member"
    }

    private suspend fun proxyLatch(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.LATCH
        database.updateSystem(system)
        return "Autoproxy mode is now set to `latch`"
    }

    private suspend fun proxyFront(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.FRONT
        database.updateSystem(system)
        return "Autoproxy mode is now set to `front`"
    }

    private suspend fun proxyMember(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.getMemberByIdAndName(system.id, ctx.params["member"]!!)
            ?: database.getMemberById(system.id, ctx.params["member"]!!)
            ?: return "Member does not exist. Create one using `pf>member new`"
        system.autoType = AutoProxyMode.MEMBER
        system.autoProxy = member.id
        database.updateSystem(system)
        return "Autoproxy mode is now set to ${member.name}"
    }

    private suspend fun proxyOff(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.autoType = AutoProxyMode.OFF
        database.updateSystem(system)
        return "Autoproxy disabled"
    }

    private suspend fun serverProxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getServerSettingsById(ctx.message.getGuild().id.value.toString(), system.id)
        return "Please tell me if you want to enable or disable proxy for this server"
    }

    private suspend fun serverProxyOn(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getServerSettingsById(ctx.message.getGuild().id.value.toString(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.LATCH
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been enabled"
    }

    private suspend fun serverProxyOff(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val systemServer = database.getServerSettingsById(ctx.message.getGuild().id.value.toString(), system.id)
        systemServer.autoProxyMode = AutoProxyMode.OFF
        database.updateSystemServerSettings(systemServer)
        return "Proxy for this server has been disabled"
    }

    private suspend fun roleEmpty(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild().id.value.toString())
        if (server.proxyRole == null) return "There is no proxy role set."
        return "Current role is <@&${server.proxyRole}>"
    }

    private suspend fun role(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild().id.value.toString())
        val role = if (Regex("<@&[0-9]*>").containsMatchIn(ctx.params["role"]!!))
            ctx.params["role"]!!.substring(3, ctx.params["role"]!!.length - 1)
        else if (Regex("[0-9]*").containsMatchIn(ctx.params["role"]!!))
            ctx.params["role"]!!
        else return "Please provide a role to set"
        server.proxyRole = role
        database.updateServerSettings(server)
        return "Role updated!"
    }

    private suspend fun roleClear(ctx: MessageHolder): String {
        val server = database.getServerSettings(ctx.message.getGuild().id.value.toString())
        server.proxyRole = null
        database.updateServerSettings(server)
        return "Role removed!"
    }
}