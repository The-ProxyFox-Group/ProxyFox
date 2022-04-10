package io.github.proxyfox.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*
import io.github.proxyfox.printStep

object SystemCommands {
    private fun changeSystemName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun accessSystemName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeSystemTag(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun accessSystemTag(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeSystemDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun accessSystemDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeSystemAvatar(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun accessSystemAvatar(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun listSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun deleteSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun createSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun createSystemNamed(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun accessSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    suspend fun register() {
        printStep("Registering system commands",2)
        commands(arrayOf("system","s")) {
            // Change system name
            val name: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemName)
                }
                executes(SystemCommands::accessSystemName)
            }
            literal("name", name)
            literal("rename", name)

            // Change system tag
            literal("tag") {
                argument("tag", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemTag)
                }
                executes(SystemCommands::accessSystemTag)
            }

            // Change system description
            val description: Node = {
                argument("description", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemDescription)
                }
                executes(SystemCommands::accessSystemDescription)
            }
            literal("description", description)
            literal("desc", description)
            literal("d", description)

            // Change system avatar
            val avatar: Node =  {
                argument("avatar", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemAvatar)
                }
                executes(SystemCommands::accessSystemAvatar)
            }
            literal("avatar", avatar)
            literal("pfp", avatar)

            // List system members
            literal("list") {
                executes(SystemCommands::listSystem)
            }

            // Delete system
            literal("delete") {
                executes(SystemCommands::deleteSystem)
            }

            // Create system
            val create: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(SystemCommands::createSystemNamed)
                }
                executes(SystemCommands::createSystem)
            }
            literal("new", create)
            literal("create", create)

            executes(SystemCommands::accessSystem)
        }
    }
}