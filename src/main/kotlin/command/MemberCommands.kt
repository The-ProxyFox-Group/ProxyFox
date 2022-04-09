package command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.steyn.brigadierkt.*

typealias Node = LiteralArgumentBuilder<CommandSource>.() -> Unit

object MemberCommands {
    private fun changeName(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeDisplayName(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }

    suspend fun register() {
        commands(arrayOf("member","m")) {
            argument("member",StringArgumentType.string()) {
                // Change member name
                val name: Node = {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeName)
                    }
                }
                literal("name",name)
                literal("rename",name)

                // Change member display name
                val displayname: Node = {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeDisplayName)
                    }
                }
                literal("displayname",displayname)
                literal("dn",displayname)
                literal("nickname",displayname)
                literal("nick",displayname)


            }
        }
    }
}
