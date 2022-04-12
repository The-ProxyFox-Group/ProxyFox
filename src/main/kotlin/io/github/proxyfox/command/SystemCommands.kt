package io.github.proxyfox.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.argument
import io.github.proxyfox.command.extension.caseInsensitiveLiteral
import io.github.proxyfox.database
import io.github.proxyfox.printStep
import io.github.proxyfox.runAsync

/**
 * Commands for accessing and changing system settings
 * @author Oliver
 * */
object SystemCommands {
    private fun changeSystemName(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to update name")
            return@runAsync
        }
        val name = StringArgumentType.getString(ctx, "name")
        system.name = name
        database.updateSystem(system)
        ctx.source.message.channel.createMessage("System name updated to `$name`")
    }

    private fun accessSystemName(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to fetch name")
            return@runAsync
        }
        val name = system.name
        ctx.source.message.channel.createMessage("Current system name is `$name`")
    }

    private fun changeSystemTag(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to update tag")
            return@runAsync
        }
        val tag = StringArgumentType.getString(ctx, "tag")
        system.tag = tag
        database.updateSystem(system)
        ctx.source.message.channel.createMessage("System tag updated to `$tag`")
    }

    private fun accessSystemTag(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to fetch tag")
            return@runAsync
        }
        val tag = system.tag
        ctx.source.message.channel.createMessage("Current system tag is `$tag`")
    }

    private fun changeSystemDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to update description")
            return@runAsync
        }
        val desc = StringArgumentType.getString(ctx, "description")
        system.tag = desc
        database.updateSystem(system)
        ctx.source.message.channel.createMessage("System description updated to `$desc`")
    }

    private fun accessSystemDescription(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to fetch description")
            return@runAsync
        }
        val desc = system.description
        ctx.source.message.channel.createMessage("Current system description is `$desc`")
    }

    private fun changeSystemAvatar(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        val isFetch = ctx.source.message.attachments.isNotEmpty()
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to " + (if (isFetch) "fetch" else "update") + " avatar")
            return@runAsync
        }
        if (isFetch) {
            val link = system.avatarUrl
            ctx.source.message.channel.createMessage("System avatar is\n$link")
            return@runAsync
        }
        val link = ctx.source.message.attachments.toList()[0].url
        system.avatarUrl = link
        database.updateSystem(system)
        ctx.source.message.channel.createMessage("System avatar updated to\n$link")
    }

    private fun changeSystemAvatarLinked(ctx: CommandContext<CommandSource>): Int = runAsync {
        val system = database.getSystemByHost(ctx.source.message.author!!.id)
        if (system == null) {
            ctx.source.message.channel.createMessage("No system registered, unable to update description")
            return@runAsync
        }
        val link = StringArgumentType.getString(ctx, "avatar")
        system.avatarUrl = link
        database.updateSystem(system)
        ctx.source.message.channel.createMessage("System description updated to `$link`")
    }

    private fun listSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun deleteSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun createSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun createSystemNamed(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    private fun accessSystem(ctx: CommandContext<CommandSource>): Int = runAsync {
        //TODO: not implemented
    }

    suspend fun register() {
        printStep("Registering system commands",2)
        commands(arrayOf("system","s")) {
            // Change system name
            val name: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemName)
                }
                executes(SystemCommands::accessSystemName)
            }
            caseInsensitiveLiteral("name", name)
            caseInsensitiveLiteral("rename", name)

            // Change system tag
            caseInsensitiveLiteral("tag") {
                argument("tag", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemTag)
                }
                executes(SystemCommands::accessSystemTag)
            }

            // Change system description
            val description: Node = {
                argument("description", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemDescription)
                }
                executes(SystemCommands::accessSystemDescription)
            }
            caseInsensitiveLiteral("description", description)
            caseInsensitiveLiteral("desc", description)
            caseInsensitiveLiteral("d", description)

            // Change system avatar
            val avatar: Node = {
                argument("avatar", StringArgumentType.greedyString()) {
                    executes(SystemCommands::changeSystemAvatarLinked)
                }
                executes(SystemCommands::changeSystemAvatar)
            }
            caseInsensitiveLiteral("avatar", avatar)
            caseInsensitiveLiteral("pfp", avatar)

            // List system members
            caseInsensitiveLiteral("list") {
                executes(SystemCommands::listSystem)
            }

            // Delete system
            caseInsensitiveLiteral("delete") {
                executes(SystemCommands::deleteSystem)
            }

            // Create system
            val create: Node = {
                argument("name", StringArgumentType.greedyString()) {
                    executes(SystemCommands::createSystemNamed)
                }
                executes(SystemCommands::createSystem)
            }
            caseInsensitiveLiteral("new", create)
            caseInsensitiveLiteral("create", create)

            executes(SystemCommands::accessSystem)
        }
    }
}