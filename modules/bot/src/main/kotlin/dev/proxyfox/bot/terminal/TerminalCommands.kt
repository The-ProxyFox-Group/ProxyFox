/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.terminal

import dev.proxyfox.common.printStep
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * Terminal related functions and variables
 * @author Oliver
 * */

object TerminalCommands {
    suspend fun start() {
        printStep("Start reading console input", 1)
        startThread()
    }

    suspend fun startThread() {
        printStep("Launching thread", 2)
        thread {
            while (true) {
                val input = readln()
                if (input.lowercase() == "exit") exitProcess(0)
            }
        }
    }
}