package io.github.proxyfox.command

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

    private fun empty(ctx: MessageHolder): String {
        TODO()
    }

    private fun createEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun create(ctx: MessageHolder): String {
        TODO()
    }

    private fun renameEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun rename(ctx: MessageHolder): String {
        TODO()
    }

    private fun accessName(ctx: MessageHolder): String {
        TODO()
    }

    private fun list(ctx: MessageHolder): String {
        TODO()
    }

    private fun listByMessage(ctx: MessageHolder): String {
        TODO()
    }

    private fun description(ctx: MessageHolder): String {
        TODO()
    }

    private fun descriptionRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun descriptionEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun avatar(ctx: MessageHolder): String {
        TODO()
    }

    private fun avatarRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun avatarEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun tag(ctx: MessageHolder): String {
        TODO()
    }

    private fun tagRaw(ctx: MessageHolder): String {
        TODO()
    }

    private fun tagEmpty(ctx: MessageHolder): String {
        TODO()
    }

    private fun delete(ctx: MessageHolder): String {
        TODO()
    }
}