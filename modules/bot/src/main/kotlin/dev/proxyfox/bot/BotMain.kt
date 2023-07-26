/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.proxyfox.api.ApiMain
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

        markdownParser.addDefaultRules()

        // Setup database
        DatabaseMain.main(findUnixValue(args, "--database="))

        // Start API
        ApiMain.main()

        // Start reading console input
        TerminalCommands.start()

        // Login to Kord
        login()
    }
}