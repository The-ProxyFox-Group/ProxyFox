package command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.steyn.brigadierkt.*

val dispatcher = CommandDispatcher<CommandSource>()

suspend fun command(literal: String, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) = dispatcher.command(literal, action)
suspend fun commands(literals: Array<String>, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
    for (literal in literals)
        command(literal, action)
}

object Commands {
    suspend fun register() {
        MemberCommands.register()
        SystemCommands.register()
    }
}