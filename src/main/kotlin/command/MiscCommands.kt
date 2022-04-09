package command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*

object MiscCommands {
    private fun getTimeString(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun getHelp(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun getExplanation(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun getInvite(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun enableServerProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun disableServerProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun enableAutoProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun disableAutoProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeProxyRole(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun removeProxyRole(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun importSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun importSystemLinked(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun exportSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    suspend fun register() {
        println("  Registering misc commands")
        command("time") {
            executes(MiscCommands::getTimeString)
        }
        commands(arrayOf("help","commands","?")) {
            executes(MiscCommands::getHelp)
        }
        commands(arrayOf("explain","why")) {
            executes(MiscCommands::getExplanation)
        }
        commands(arrayOf("invite","link")) {
            executes(MiscCommands::getInvite)
        }

        commands(arrayOf("proxy","serverproxy")) {
            literal("on") {
                executes(MiscCommands::enableServerProxy)
            }
            literal("off") {
                executes(MiscCommands::disableServerProxy)
            }
            executes(::noSubCommandError)
        }

        commands(arrayOf("autoproxy","ap")) {
            literal("on") {
                executes(MiscCommands::enableAutoProxy)
            }
            literal("off") {
                executes(MiscCommands::disableAutoProxy)
            }
            executes(::noSubCommandError)
        }

        command("role") {
            argument("role",StringArgumentType.greedyString()) {
                executes(MiscCommands::changeProxyRole)
            }
            literal("clear") {
                executes(MiscCommands::removeProxyRole)
            }
            executes(::noSubCommandError)
        }

        command("import") {
            argument("link",StringArgumentType.greedyString()) {
                executes(MiscCommands::importSystemLinked)
            }
            executes(MiscCommands::importSystem)
        }

        command("export") {
            executes(MiscCommands::exportSystem)
        }
    }
}