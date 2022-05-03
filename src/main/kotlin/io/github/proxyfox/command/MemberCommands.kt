package io.github.proxyfox.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.argument
import io.github.proxyfox.printStep
import io.github.proxyfox.runAsync

/**
 * Commands for accessing and changing system member settings
 * @author Oliver
 * */
object MemberCommands {
    private fun changeName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeDisplayName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessDisplayName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeServerName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessServerName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeAvatar(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeAvatarLinked(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeServerAvatar(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeServerAvatarLinked(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun addProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun removeProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessProxy(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeMemberDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessMemberDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeMemberPronouns(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessMemberPronouns(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeMemberColor(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessMemberColor(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun changeMemberBirthday(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessMemberBirthday(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun deleteMember(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessMember(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun createMember(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    suspend fun register() {
        printStep("Registering member commands", 2)
        commands(arrayOf("member", "m")) {
            argument("member", StringArgumentType.string()) {
                // Change member name
                val name: Node = {
                    argument("name", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeName)
                    }
                    executes(MemberCommands::accessName)
                }
                caseInsensitiveLiteral("name", name)
                caseInsensitiveLiteral("rename", name)

                // Change member display name
                val displayname: Node = {
                    argument("name", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeDisplayName)
                    }
                    executes(MemberCommands::accessDisplayName)
                }
                caseInsensitiveLiteral("displayname", displayname)
                caseInsensitiveLiteral("dn", displayname)
                caseInsensitiveLiteral("nickname", displayname)
                caseInsensitiveLiteral("nick", displayname)

                // Change member server nickname
                caseInsensitiveLiteral("servernick") {
                    argument("name", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeServerName)
                    }
                    executes(MemberCommands::accessServerName)
                }

                // Change member avatar
                val avatar: Node = {
                    argument("link", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeAvatarLinked)
                    }
                    executes(MemberCommands::changeAvatar)
                }
                caseInsensitiveLiteral("avatar", avatar)
                caseInsensitiveLiteral("pfp", avatar)

                // Change member server avatar
                val serveravatar: Node = {
                    argument("link", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeServerAvatarLinked)
                    }
                    executes(MemberCommands::changeServerAvatar)
                }
                caseInsensitiveLiteral("serveravatar", serveravatar)
                caseInsensitiveLiteral("serverpfp", serveravatar)

                // Edit member proxy
                caseInsensitiveLiteral("proxy") {
                    caseInsensitiveLiteral("add") {
                        argument("proxy", StringArgumentType.greedyString()) {
                            executes(MemberCommands::addProxy)
                        }
                        executes(::noSubCommandError)
                    }
                    caseInsensitiveLiteral("remove") {
                        argument("proxy", StringArgumentType.greedyString()) {
                            executes(MemberCommands::removeProxy)
                        }
                        executes(::noSubCommandError)
                    }
                    argument("proxy", StringArgumentType.greedyString()) {
                        executes(MemberCommands::addProxy)
                    }
                    executes(MemberCommands::accessProxy)
                }

                // Change member description
                val description: Node = {
                    argument("description", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberDescription)
                    }
                    executes(MemberCommands::accessMemberDescription)
                }
                caseInsensitiveLiteral("description", description)
                caseInsensitiveLiteral("desc", description)
                caseInsensitiveLiteral("d", description)

                // Change member pronouns
                caseInsensitiveLiteral("pronouns") {
                    argument("pronouns", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberPronouns)
                    }
                    executes(MemberCommands::accessMemberPronouns)
                }

                // Change member color
                caseInsensitiveLiteral("color") {
                    argument("color", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberColor)
                    }
                    executes(MemberCommands::accessMemberColor)
                }

                // Change member birthday
                caseInsensitiveLiteral("birthday") {
                    argument("birthday", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberBirthday)
                    }
                    executes(MemberCommands::accessMemberBirthday)
                }

                // Delete member
                caseInsensitiveLiteral("delete") {
                    executes(MemberCommands::deleteMember)
                }

                // Access member
                executes(MemberCommands::accessMember)
            }

            // Create member
            val create: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(MemberCommands::createMember)
                }
            }
            caseInsensitiveLiteral("new", create)
            caseInsensitiveLiteral("add", create)
            caseInsensitiveLiteral("create", create)

            executes(::noSubCommandError)
        }
    }
}
