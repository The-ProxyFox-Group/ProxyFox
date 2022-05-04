@file:JvmName("Main")

package io.github.proxyfox

import io.github.proxyfox.api.RestApi
import io.github.proxyfox.terminal.TerminalCommands


/**
 * @author Oliver
 * */
suspend fun main() {


    // Hack to not get io.ktor.random warning
    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")

    printFancy("Initializing ProxyFox")

//    // Register commands in brigadier
//    Commands.register()

    // Setup database
    setupDatabase()

    // Start reading console input
    TerminalCommands.start()

    // Start REST API
    RestApi.start()

    // Login to Kord
    login()
}