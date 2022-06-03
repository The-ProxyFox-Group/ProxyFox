@file:JvmName("Main")

package dev.proxyfox

import dev.proxyfox.api.RestApi
import dev.proxyfox.command.Commands
import dev.proxyfox.terminal.TerminalCommands


/**
 * @author Oliver
 * */
suspend fun main() {
    // Hack to not get io.ktor.random warning
    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")

    printFancy("Initializing ProxyFox")

    // Register commands
    Commands.register()

    // Setup database
    setupDatabase()

    // Start reading console input
    TerminalCommands.start()

    // Start REST API
    RestApi.start()

    // Login to Kord
    login()
}