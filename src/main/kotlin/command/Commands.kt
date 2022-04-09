package command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*

val dispatcher = CommandDispatcher<CommandSource>()

typealias Node = LiteralArgumentBuilder<CommandSource>.() -> Unit

suspend fun command(literal: String, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) = dispatcher.command(literal, action)
suspend fun commands(literals: Array<String>, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
    for (literal in literals)
        command(literal, action)
}

fun noSubCommandError(ctx: CommandContext<CommandSource>): Int {
    //TODO: not implemented
    return 0
}

object Commands {
    suspend fun register() {
        println(" Registering commands")
        SystemCommands.register()
        MemberCommands.register()
        MiscCommands.register()
    }
}