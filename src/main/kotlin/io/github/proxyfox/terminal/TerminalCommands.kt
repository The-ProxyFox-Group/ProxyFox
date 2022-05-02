package io.github.proxyfox.terminal

import com.mojang.brigadier.CommandDispatcher
import dev.steyn.brigadierkt.command
import io.github.proxyfox.printStep
import io.github.proxyfox.runAsync
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * Terminal related functions and variables
 * @author Oliver
 * */
val terminalDispatcher = CommandDispatcher<TerminalCommandSource>()

object TerminalCommands {
    suspend fun start() {
        printStep("Start reading console input", 1)
        register()
        startThread()
    }

    suspend fun startThread() {
        printStep("Launching thread", 2)
        thread {
            runAsync {
                while (true) {
                    val input = readln()
                    terminalDispatcher.execute(input, TerminalCommandSource())
                }
            }
        }
    }

    suspend fun register() {
        printStep("Registering commands", 2)
        terminalDispatcher.command("stop") {
            executes {
                exitProcess(0)
            }
        }
    }
}

class TerminalCommandSource