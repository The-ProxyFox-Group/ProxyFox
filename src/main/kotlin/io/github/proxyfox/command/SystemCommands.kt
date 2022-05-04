package io.github.proxyfox.command

import io.github.proxyfox.printStep

/**
 * Commands for accessing and changing system settings
 * @author Oliver
 * */
object SystemCommands {
    suspend fun register() {
        printStep("Registering system commands", 2)
    }
}