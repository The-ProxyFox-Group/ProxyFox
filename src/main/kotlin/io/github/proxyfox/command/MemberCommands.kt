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

                val nickname: CommandNode = {
                    greedy("name", ::nicknameMember)
                }
                literal("nickname", ::nicknameMemberEmpty, nickname)
                literal("nick", ::nicknameMemberEmpty, nickname)
                literal("displayname", ::nicknameMemberEmpty, nickname)
                literal("dn", ::nicknameMemberEmpty, nickname)

                val servername: CommandNode = {
                    greedy("name", ::servernameMember)
                }
                literal("servername", ::servernameMemberEmpty, servername)
                literal("servernick", ::servernameMemberEmpty, servername)

                val desc: CommandNode = {
                    literal("-raw", ::memberDescriptionRaw)
                    greedy("desc", ::memberDescription)
                }
                literal("desc", ::memberDescriptionEmpty, desc)
                literal("description", ::memberDescriptionEmpty, desc)

                val avatar: CommandNode = {
                    greedy("avatar", ::memberAvatarLinked)
                }
                literal("avatar", ::memberAvatar, avatar)
                literal("pfp", ::memberAvatar, avatar)

                val serveravatar: CommandNode = {
                    greedy("avatar", ::memberServerAvatarLinked)
                }
                literal("serveravatar", ::memberServerAvatar, serveravatar)
                literal("serverpfp", ::memberServerAvatar, serveravatar)

                literal("proxy", ::memberProxyEmpty) {
                    literal("remove", ::memberRemoveProxyEmpty) {
                        greedy("proxy", ::memberRemoveProxy)
                    }
                    greedy("proxy", ::memberProxy)
                }

                literal("pronouns", ::memberPronounsEmpty) {
                    literal("-raw", ::memberPronounsRaw)
                    greedy("pronouns", ::memberPronouns)
                }

                literal("color", ::memberColorEmpty) {
                    literal("-raw", ::memberColorRaw)
                    greedy("color", ::memberColor)
                }

                literal("birthday", ::memberBirthEmpty) {
                    literal("-raw", ::memberBirthRaw)
                    greedy("birthday", ::memberBirth)
                }

                literal("delete", ::memberDelete)
            }

            literal("new", ::memberCreateEmpty) {
                greedy("name", ::memberCreate)
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

    private fun nicknameMemberEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun nicknameMember(ctx: MessageHolder): String {
        TODO()
    }

    private fun servernameMemberEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun servernameMember(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberDescriptionEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberDescriptionRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberDescription(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberAvatarLinked(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberAvatar(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberServerAvatarLinked(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberServerAvatar(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberRemoveProxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberRemoveProxy(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberProxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberProxy(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberPronounsEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberPronounsRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberPronouns(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberColorEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberColorRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberColor(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberBirthEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberBirthRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberBirth(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberDelete(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberCreateEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun memberCreate(ctx: MessageHolder): String {
        TODO()
    }
}
