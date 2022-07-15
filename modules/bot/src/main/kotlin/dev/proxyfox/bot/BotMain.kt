@file:JvmName("Main")

package dev.proxyfox.bot

import dev.proxyfox.bot.command.Commands
import dev.proxyfox.bot.terminal.TerminalCommands
import dev.proxyfox.common.printFancy
import dev.proxyfox.database.DatabaseMain

suspend fun main(args: Array<String>) = BotMain.main(args)

/**
 * @author Oliver
 * */
object BotMain {
    suspend fun main(args: Array<String>) {
        // Hack to not get io.ktor.random warning
        System.setProperty("io.ktor.random.secure.random.provider", "DRBG")

        printFancy("Initializing ProxyFox")

        // Register commands
        Commands.register()

        // Setup database
        DatabaseMain.main(findUnixValue(args, "--database="))

        // Start reading console input
        TerminalCommands.start()

        // Login to Kord
        login()
    }
}