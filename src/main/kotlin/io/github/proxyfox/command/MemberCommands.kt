package io.github.proxyfox.command

import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.dsl.string
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand

/**
 * Commands for accessing and changing system member settings
 * @author Oliver
 * */
object MemberCommands {

    suspend fun register() {
        printStep("Registering member commands", 2)
        val memberCommands: CommandNode = {
            string("member", ::accessMember) {
                val name: CommandNode = {
                    greedy("name", ::renameMember)
                }
                literal("rename", ::renameMemberEmpty, name)
                literal("name", ::renameMemberEmpty, name)
            }

        }
        registerCommand(literal("member", ::emptyMember, memberCommands))
    }

    private fun emptyMember(ctx: MessageHolder): String {
        TODO()
    }

    private fun accessMember(ctx: MessageHolder): String {
        TODO()
    }

    private fun renameMemberEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun renameMember(ctx: MessageHolder): String {
        TODO()
    }
}
