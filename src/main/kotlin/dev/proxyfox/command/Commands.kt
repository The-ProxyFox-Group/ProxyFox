package dev.proxyfox.command

import dev.proxyfox.printStep
import dev.proxyfox.string.node.Node

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