package command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*

object MemberCommands {
    private fun changeName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeDisplayName(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeServerName(ctx: CommandContext<CommandSource>): Int = runAsync {
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
    private fun changeMemberDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeMemberPronouns(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeMemberColor(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }
    private fun changeMemberBirthday(ctx: CommandContext<CommandSource>): Int = runAsync {
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
        println("  Registering member commands")
        commands(arrayOf("member","m")) {
            argument("member",StringArgumentType.string()) {
                // Change member name
                val name: Node = {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeName)
                    }
                    executes(::noSubCommandError)
                }
                literal("name", name)
                literal("rename", name)

                // Change member display name
                val displayname: Node = {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeDisplayName)
                    }
                    executes(::noSubCommandError)
                }
                literal("displayname", displayname)
                literal("dn", displayname)
                literal("nickname", displayname)
                literal("nick", displayname)

                // Change member server nickname
                literal("servernick") {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeServerName)
                    }
                    executes(::noSubCommandError)
                }

                // Change member avatar
                val avatar: Node = {
                    argument("link", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeAvatarLinked)
                    }
                    executes(MemberCommands::changeAvatar)
                }
                literal("avatar", avatar)
                literal("pfp", avatar)

                // Change member server avatar
                val serveravatar: Node = {
                    argument("link", StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeServerAvatarLinked)
                    }
                    executes(MemberCommands::changeServerAvatar)
                }
                literal("serveravatar", serveravatar)
                literal("serverpfp", serveravatar)

                // Edit member proxy
                literal("proxy") {
                    literal("add") {
                        argument("proxy",StringArgumentType.greedyString()) {
                            executes(MemberCommands::addProxy)
                        }
                        executes(::noSubCommandError)
                    }
                    literal("remove") {
                        argument("proxy",StringArgumentType.greedyString()) {
                            executes(MemberCommands::removeProxy)
                        }
                        executes(::noSubCommandError)
                    }
                    argument("proxy",StringArgumentType.greedyString()) {
                        executes(MemberCommands::addProxy)
                    }
                    executes(::noSubCommandError)
                }

                // Change member description
                literal("description") {
                    argument("description",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberDescription)
                    }
                    executes(::noSubCommandError)
                }

                // Change member pronouns
                literal("pronouns") {
                    argument("pronouns",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberPronouns)
                    }
                    executes(::noSubCommandError)
                }

                // Change member color
                literal("color") {
                    argument("color",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberColor)
                    }
                    executes(::noSubCommandError)
                }

                // Change member birthday
                literal("birthday") {
                    argument("birthday",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeMemberBirthday)
                    }
                    executes(::noSubCommandError)
                }

                // Delete member
                literal("delete") {
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
            literal("new",create)
            literal("add",create)
            literal("create",create)

            executes(::noSubCommandError)
        }
    }
}
