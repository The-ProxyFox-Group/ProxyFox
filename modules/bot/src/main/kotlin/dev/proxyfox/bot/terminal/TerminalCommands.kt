/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.terminal

import dev.proxyfox.command.CommandParser
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.common.printStep
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * Terminal related functions and variables
 * @author Oliver
 * */
object TerminalCommands {
    val parser = CommandParser<String, TerminalContext>()

    suspend fun start() {
        printStep("Start reading console input", 1)
        parser.literal("exit", "stop", "quit") {
            executes {
                exitProcess(0)
            }
        }
        startThread()
    }

    private fun startThread() {
        printStep("Launching thread", 2)
        thread {
            runBlocking {
                while (true) {
                    val input = readln()
                    parser.parse(TerminalContext(input))
                }
            }
        }
    }
}