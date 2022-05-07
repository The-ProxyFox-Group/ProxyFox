package io.github.proxyfox.command

import io.github.proxyfox.database
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

    private suspend fun empty(ctx: MessageHolder): String {
        return "Make sur eto provide a member command!"
    }

    private suspend fun access(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun renameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun rename(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun nicknameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun nickname(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun servernameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun servername(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun descriptionEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun descriptionRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun description(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun avatarLinked(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun avatar(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun serverAvatarLinked(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun serverAvatar(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun removeProxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun removeProxy(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun proxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun proxy(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun pronounsEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun pronounsRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun pronouns(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun colorEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun colorRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun color(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun birthEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun birthRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun birth(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        TODO()
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.getMemberById(system.id, ctx.params["member"]!!)
            ?: return "Member does not exist. Create one using `pf>member new`"
        TODO()
    }

    private suspend fun createEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to provide a name for the new member!"
    }

    private suspend fun create(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        database.allocateMember(system.id, ctx.params["name"]!!)
        return "Member created!"
    }
}
