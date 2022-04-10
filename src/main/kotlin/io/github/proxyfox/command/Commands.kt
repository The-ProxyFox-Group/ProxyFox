package io.github.proxyfox.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.steyn.brigadierkt.*
import io.github.proxyfox.printStep
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val dispatcher = CommandDispatcher<CommandSource>()

typealias Node = LiteralArgumentBuilder<CommandSource>.() -> Unit

suspend fun command(literal: String, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) = dispatcher.command(literal, action)
suspend fun commands(literals: Array<String>, action: LiteralArgumentBuilder<CommandSource>.() -> Unit) {
    for (literal in literals)
        command(literal, action)
}

@OptIn(DelicateCoroutinesApi::class)
fun runAsync(action: suspend () -> Unit): Int {
    GlobalScope.launch {
        action()
    }
    return 0
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