package io.github.proxyfox.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.command
import io.github.proxyfox.command.extension.CaseInsensitiveLiteralArgumentBuilder
import io.github.proxyfox.printStep
import io.github.proxyfox.runAsync

val dispatcher = CommandDispatcher<CommandSource>()

typealias Node = CaseInsensitiveLiteralArgumentBuilder<CommandSource>.() -> Unit

suspend fun command(literal: String, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) = dispatcher.command(literal, action)
suspend fun commands(literals: Array<String>, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
    for (literal in literals)
        command(literal, action)
}

fun noSubCommandError(ctx: CommandContext<CommandSource>): Int = runAsync {
    ctx.source.message.channel.createMessage("No subcommand given")
}

object Commands {
    suspend fun register() {
        printStep("Registering commands",1)
        SystemCommands.register()
        MemberCommands.register()
        MiscCommands.register()
    }
}