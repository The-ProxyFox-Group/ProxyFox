package command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.steyn.brigadierkt.*

class CommandSource {

}

val dispatcher = CommandDispatcher<CommandSource>()

fun command(literal: String, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) = dispatcher.command(literal, action)
fun commands(literals: Array<String>, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
    for (literal in literals) {
        command(literal, action)
    }
}

fun register() {
    MemberCommands.register()
    SystemCommands.register()
}