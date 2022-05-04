package io.github.proxyfox.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.proxyfox.command.extension.CaseInsensitiveLiteralArgumentBuilder
import io.github.proxyfox.printStep
import io.github.proxyfox.runAsync

/**
 * General utilities relating to commands
 * @author Oliver
 * */

val dispatcher = CommandDispatcher<CommandSource>()

typealias Node = CaseInsensitiveLiteralArgumentBuilder<CommandSource>.() -> Unit

suspend fun command(
    literal: String,
    action: CaseInsensitiveLiteralArgumentBuilder<CommandSource>.() -> Unit
): CaseInsensitiveLiteralArgumentBuilder<CommandSource> {
    val literal = CaseInsensitiveLiteralArgumentBuilder.literal<CommandSource>(literal).apply(action)
    dispatcher.root.addChild(
        literal.build()
    )
    return literal
}

suspend fun commands(literals: Array<String>, action: CaseInsensitiveLiteralArgumentBuilder<CommandSource>.() -> Unit) {
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