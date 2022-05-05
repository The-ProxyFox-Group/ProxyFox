package io.github.proxyfox.command

import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand

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

    private fun importEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun import(ctx: MessageHolder): String {
        TODO()
    }

    private fun export(ctx: MessageHolder): String {
        TODO()
    }

    private fun time(ctx: MessageHolder): String {
        TODO()
    }

    private fun help(ctx: MessageHolder): String {
        TODO()
    }

    private fun explain(ctx: MessageHolder): String {
        TODO()
    }

    private fun invite(ctx: MessageHolder): String {
        TODO()
    }

    private fun source(ctx: MessageHolder): String {
        TODO()
    }

    private fun proxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun proxyOn(ctx: MessageHolder): String {
        TODO()
    }

    private fun proxyOff(ctx: MessageHolder): String {
        TODO()
    }

    private fun serverProxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun serverProxyOn(ctx: MessageHolder): String {
        TODO()
    }

    private fun serverProxyOff(ctx: MessageHolder): String {
        TODO()
    }

    private fun roleEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun role(ctx: MessageHolder): String {
        TODO()
    }

    private fun roleClear(ctx: MessageHolder): String {
        TODO()
    }
}