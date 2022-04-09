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
    private fun changeServerName(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeAvatar(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeAvatarLinked(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeServerAvatar(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeServerAvatarLinked(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun addProxy(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun removeProxy(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeMemberDescription(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeMemberPronouns(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeMemberColor(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun changeMemberBirthday(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun deleteMember(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun accessMember(ctx: CommandContext<CommandSource>): Int {
        //TODO: not implemented
        return 0
    }
    private fun createMember(ctx: CommandContext<CommandSource>): Int {
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
                literal("name", name)
                literal("rename", name)

                // Change member display name
                val displayname: Node = {
                    argument("name",StringArgumentType.greedyString()) {
                        executes(MemberCommands::changeDisplayName)
                    }
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
                    }
                    literal("remove") {
                        argument("proxy",StringArgumentType.greedyString()) {
                            executes(MemberCommands::removeProxy)
                        }
                    }
                    argument("proxy",StringArgumentType.greedyString()) {
                        executes(MemberCommands::addProxy)
                    }
                }

                // Change member description
                literal("description") {
                    literal("description") {
                        executes(MemberCommands::changeMemberDescription)
                    }
                }

                // Change member pronouns
                literal("pronouns") {
                    literal("pronouns") {
                        executes(MemberCommands::changeMemberPronouns)
                    }
                }

                // Change member color
                literal("color") {
                    literal("color") {
                        executes(MemberCommands::changeMemberColor)
                    }
                }

                // Change member birthday
                literal("birthday") {
                    literal("birthday") {
                        executes(MemberCommands::changeMemberBirthday)
                    }
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
        }
    }
}
