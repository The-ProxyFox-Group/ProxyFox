package dev.proxyfox.command

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.database
import dev.proxyfox.kord
import dev.proxyfox.printStep
import dev.proxyfox.string.dsl.greedy
import dev.proxyfox.string.dsl.literal
import dev.proxyfox.string.parser.MessageHolder
import dev.proxyfox.string.parser.registerCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter

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
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val members = database.getTotalMembersByHost(ctx.message.author!!.id.value.toString())!!
        ctx.message.channel.createMessage {
            embed {
                title = "${system.name} [`${system.id}`]"
                val avatar = system.avatarUrl
                if (avatar != null) thumbnail {
                    url = avatar
                }
                val tag = system.tag
                if (tag != null) field {
                    name = "Tag"
                    value = tag
                    inline = true
                }
                field {
                    name = "Members (`${members}`)"
                    value = "See `pf>system list`"
                    inline = true
                }
                val description = system.description
                if (description != null) field {
                    name = "Description"
                    value = description
                }
                footer {
                    val formatter = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm:ss")
                    text = "Created on ${formatter.format(system.timestamp)}"
                }
            }
        }
        return ""
    }

    private suspend fun createEmpty(ctx: MessageHolder): String {
        database.allocateSystem(ctx.message.author!!.id.value.toString())
        return "System created! See `pf>help` for how to set up your system further!"
    }

    private suspend fun create(ctx: MessageHolder): String {
        val system = database.allocateSystem(ctx.message.author!!.id.value.toString())
        system.name = ctx.params["name"]
        database.updateSystem(system)
        return "System created with name ${system.name}! See `pf>help` for how to set up your system further!"
    }

    private suspend fun renameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to provide me with a name to update your system!"
    }

    private suspend fun rename(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.name = ctx.params["name"]
        database.updateSystem(system)
        return "System name updated to ${system.name}!"
    }

    private suspend fun accessName(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "System's name is ${system.name}"
    }

    private suspend fun list(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"

        return ""
    }

    private suspend fun listByMessage(ctx: MessageHolder): String {
        // TODO: Make it sort by message count
        return list(ctx)
    }

    private suspend fun description(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.description = ctx.params["desc"]
        database.updateSystem(system)
        return "Description updated!"
    }

    private suspend fun descriptionRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.description}`"
    }

    private suspend fun descriptionEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.description!!
    }

    private suspend fun avatar(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.avatarUrl = ctx.params["avatar"]
        database.updateSystem(system)
        return "System avatar updated!"
    }

    private suspend fun avatarRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.avatarUrl}`"
    }

    private suspend fun avatarEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.avatarUrl!!
    }

    private suspend fun tag(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        system.tag = ctx.params["tag"]
        database.updateSystem(system)
        return "System tag updated!"
    }

    private suspend fun tagRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.tag}`"
    }

    private suspend fun tagEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.tag!!
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author!!.id.value.toString())
            ?: return "System does not exist. Create one using `pf>system new`"
        val message1 =
            ctx.message.channel.createMessage("Are you sure you want to delete your system?\nThe data will be lost forever (A long time!)")
        message1.addReaction(ReactionEmoji.Unicode("❌"))
        message1.addReaction(ReactionEmoji.Unicode("✅"))
        var job: Job? = null
        job = kord.on<ReactionAddEvent> {
            if (message.id == message1.id) {
                message.getReactors(ReactionEmoji.Unicode("❌")).map {
                    if (it.id == ctx.message.author!!.id) {
                        message.channel.createMessage("Member deleted")
                        job!!.cancel()
                    }
                }
                message.getReactors(ReactionEmoji.Unicode("✅")).map {
                    if (it.id == ctx.message.author!!.id) {
                        message.channel.createMessage("Action cancelled")
                        job!!.cancel()
                    }
                }
            }
        }
        return ""
    }
}