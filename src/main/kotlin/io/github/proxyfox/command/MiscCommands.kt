package io.github.proxyfox.command

import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand
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
            literal("on", ::proxyOn)
            literal("enable", ::proxyOn)
        }
        registerCommand(literal("autoproxy", ::proxyEmpty, autoproxy))
        registerCommand(literal("ap", ::proxyEmpty, autoproxy))

        registerCommand(literal("role", ::roleEmpty) {
            literal("clear", ::roleClear)
            greedy("role", ::role)
        })
    }

    private suspend fun importEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun import(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun export(ctx: MessageHolder): String {
        TODO()
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
        TODO()
    }

    private suspend fun proxyOn(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun proxyOff(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun serverProxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun serverProxyOn(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun serverProxyOff(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun roleEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun role(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun roleClear(ctx: MessageHolder): String {
        TODO()
    }
}