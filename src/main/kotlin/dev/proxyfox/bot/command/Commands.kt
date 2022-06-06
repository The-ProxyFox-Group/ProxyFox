package dev.proxyfox.bot.command

import dev.proxyfox.common.printStep
import dev.proxyfox.bot.string.node.Node

/**
 * General utilities relating to commands
 * @author Oliver
 * */

typealias CommandNode = suspend Node.() -> Unit

object Commands {
    suspend fun register() {
        printStep("Registering commands",1)
        SystemCommands.register()
        MemberCommands.register()
        MiscCommands.register()
    }
}