package io.github.proxyfox.command

import io.github.proxyfox.database
import io.github.proxyfox.printStep
import io.github.proxyfox.string.dsl.greedy
import io.github.proxyfox.string.dsl.literal
import io.github.proxyfox.string.parser.MessageHolder
import io.github.proxyfox.string.parser.registerCommand

/**
 * Commands for accessing and changing system settings
 * @author Oliver
 * */
object SystemCommands {
    suspend fun register() {
        printStep("Registering system commands", 2)
        val system: CommandNode = {
            val new: CommandNode = {
                greedy("name", ::create)
            }
            literal("new", ::createEmpty, new)
            literal("create", ::createEmpty, new)
            literal("add", ::createEmpty, new)

            val name: CommandNode = {
                greedy("name", ::rename)
            }
            literal("rename", ::renameEmpty, name)
            literal("name", ::accessName, name)

            val list: CommandNode = {
                literal("-by-message-count", ::listByMessage)
            }
            literal("list", ::list, list)
            literal("l", ::list, list)

            val desc: CommandNode = {
                literal("-raw", ::descriptionRaw)
                greedy("desc", ::description)
            }
            literal("description", ::descriptionEmpty, desc)
            literal("desc", ::descriptionEmpty, desc)
            literal("d", ::descriptionEmpty, desc)

            val avatar: CommandNode = {
                literal("-raw", ::avatarRaw)
                greedy("avatar", ::avatar)
            }
            literal("avatar", ::avatarEmpty, avatar)
            literal("pfp", ::avatarEmpty, avatar)

            literal("tag", ::tagEmpty) {
                literal("-raw", ::tagRaw)
                greedy("tagr", ::tag)
            }

            literal("delete", ::delete)
        }
        registerCommand(literal("system", ::empty, system))
        registerCommand(literal("s", ::empty, system))
    }

    private suspend fun empty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun createEmpty(ctx: MessageHolder): String {
        database.allocateSystem(ctx.message.author!!.id)
        return "System created! See `pf>help` for how to set up your system further!"
    }

    private suspend fun create(ctx: MessageHolder): String {
        val system = database.allocateSystem(ctx.message.author!!.id)
        system.name = ctx.params["name"]
        database.updateSystem(system)
        return "System created with name ${system.name}! See `pf>help` for how to set up your system further!"
    }

    private suspend fun renameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to provide me with a name to update your system!"
    }

    private suspend fun rename(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.name = ctx.params["name"]
        database.updateSystem(system)
        return "System name updated to ${system.name}!"
    }

    private suspend fun accessName(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "System's name is ${system.name}"
    }

    private suspend fun list(ctx: MessageHolder): String {

        return ""
    }

    private suspend fun listByMessage(ctx: MessageHolder): String {

        return ""
    }

    private suspend fun description(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.description = ctx.params["desc"]
        database.updateSystem(system)
        return "Description updated!"
    }

    private suspend fun descriptionRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.description}`"
    }

    private suspend fun descriptionEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.description!!
    }

    private suspend fun avatar(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun avatarRaw(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun avatarEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun tag(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun tagRaw(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun tagEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private suspend fun delete(ctx: MessageHolder): String {
        TODO()
    }
}