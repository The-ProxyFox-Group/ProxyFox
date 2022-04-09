package command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*

object SystemCommands {
    private fun changeSystemName(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeSystemTag(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeSystemDescription(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeSystemAvatar(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun listSystem(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun deleteSystem(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun accessSystem(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun createSystem(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }

    suspend fun register() {
        println("  Registering system commands")
        commands(arrayOf("system","s")) {
            // Change system name
            val name: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemName)
                }
                executes(::noSubCommandError)
            }
            literal("name", name)
            literal("rename", name)

            // Change system tag
            literal("tag") {
                argument("tag", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemTag)
                }
                executes(::noSubCommandError)
            }

            // Change system description
            literal("description") {
                argument("description", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemDescription)
                }
                executes(::noSubCommandError)
            }

            // Change system avatar
            val avatar: Node =  {
                argument("avatar", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemAvatar)
                }
                executes(::noSubCommandError)
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
                    executes(SystemCommands::createSystem)
                }
                executes(::noSubCommandError)
            }
            literal("new", create)
            literal("create", create)

            executes(SystemCommands::accessSystem)
        }
    }
}