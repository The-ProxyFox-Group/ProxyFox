package io.github.proxyfox.command

import com.mojang.brigadier.CommandDispatcher
import io.github.proxyfox.printStep
import io.github.proxyfox.string.node.Node

/**
 * General utilities relating to commands
 * @author Oliver
 * */

val dispatcher = CommandDispatcher<CommandSource>()

typealias CommandNode = Node.() -> Unit

object Commands {
    suspend fun register() {
        printStep("Registering commands",1)
        SystemCommands.register()
        MemberCommands.register()
        MiscCommands.register()
    }
}