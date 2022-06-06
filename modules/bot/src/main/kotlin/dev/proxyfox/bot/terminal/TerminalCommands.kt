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