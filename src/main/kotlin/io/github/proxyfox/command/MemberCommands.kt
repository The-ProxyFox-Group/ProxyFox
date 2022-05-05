package io.github.proxyfox.command

import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.dsl.string
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand

/**
 * Commands for accessing and changing system  settings
 * @author Oliver
 * */
object MemberCommands {
    suspend fun register() {
        printStep("Registering  commands", 2)
        val commands: CommandNode = {
            string("", ::access) {
                val name: CommandNode = {
                    greedy("name", ::rename)
                }
                literal("rename", ::renameEmpty, name)
                literal("name", ::renameEmpty, name)

                val nickname: CommandNode = {
                    greedy("name", ::nickname)
                }
                literal("nickname", ::nicknameEmpty, nickname)
                literal("nick", ::nicknameEmpty, nickname)
                literal("displayname", ::nicknameEmpty, nickname)
                literal("dn", ::nicknameEmpty, nickname)

                val servername: CommandNode = {
                    greedy("name", ::servername)
                }
                literal("servername", ::servernameEmpty, servername)
                literal("servernick", ::servernameEmpty, servername)

                val desc: CommandNode = {
                    literal("-raw", ::descriptionRaw)
                    greedy("desc", ::description)
                }
                literal("desc", ::descriptionEmpty, desc)
                literal("description", ::descriptionEmpty, desc)
                literal("d", ::descriptionEmpty, desc)

                val avatar: CommandNode = {
                    greedy("avatar", ::avatarLinked)
                }
                literal("avatar", ::avatar, avatar)
                literal("pfp", ::avatar, avatar)

                val serveravatar: CommandNode = {
                    greedy("avatar", ::serverAvatarLinked)
                }
                literal("serveravatar", ::serverAvatar, serveravatar)
                literal("serverpfp", ::serverAvatar, serveravatar)

                literal("proxy", ::proxyEmpty) {
                    literal("remove", ::removeProxyEmpty) {
                        greedy("proxy", ::removeProxy)
                    }
                    greedy("proxy", ::proxy)
                }

                literal("pronouns", ::pronounsEmpty) {
                    literal("-raw", ::pronounsRaw)
                    greedy("pronouns", ::pronouns)
                }

                literal("color", ::colorEmpty) {
                    literal("-raw", ::colorRaw)
                    greedy("color", ::color)
                }

                literal("birthday", ::birthEmpty) {
                    literal("-raw", ::birthRaw)
                    greedy("birthday", ::birth)
                }

                literal("delete", ::delete)
            }

            literal("new", ::createEmpty) {
                greedy("name", ::create)
            }

        }
        registerCommand(literal("", ::empty, commands))
        registerCommand(literal("m", ::empty, commands))
    }

    private fun empty(ctx: MessageHolder): String {
        TODO()
    }

    private fun access(ctx: MessageHolder): String {
        TODO()
    }

    private fun renameEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun rename(ctx: MessageHolder): String {
        TODO()
    }

    private fun nicknameEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun nickname(ctx: MessageHolder): String {
        TODO()
    }

    private fun servernameEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun servername(ctx: MessageHolder): String {
        TODO()
    }

    private fun descriptionEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun descriptionRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun description(ctx: MessageHolder): String {
        TODO()
    }

    private fun avatarLinked(ctx: MessageHolder): String {
        TODO()
    }

    private fun avatar(ctx: MessageHolder): String {
        TODO()
    }

    private fun serverAvatarLinked(ctx: MessageHolder): String {
        TODO()
    }

    private fun serverAvatar(ctx: MessageHolder): String {
        TODO()
    }

    private fun removeProxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun removeProxy(ctx: MessageHolder): String {
        TODO()
    }

    private fun proxyEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun proxy(ctx: MessageHolder): String {
        TODO()
    }

    private fun pronounsEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun pronounsRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun pronouns(ctx: MessageHolder): String {
        TODO()
    }

    private fun colorEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun colorRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun color(ctx: MessageHolder): String {
        TODO()
    }

    private fun birthEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun birthRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun birth(ctx: MessageHolder): String {
        TODO()
    }

    private fun delete(ctx: MessageHolder): String {
        TODO()
    }

    private fun createEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun create(ctx: MessageHolder): String {
        TODO()
    }
}
