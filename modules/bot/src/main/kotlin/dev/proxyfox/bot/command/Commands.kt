/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.rest.builder.interaction.*
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.command.CommandParser
import dev.proxyfox.common.printStep

/**
 * General utilities relating to commands
 * @author Oliver
 * */

object Commands {
    val parser = CommandParser<Any, DiscordContext<Any>>()

    suspend fun register() {
        printStep("Registering commands",1)
        SystemCommands.register()
        MemberCommands.register()
        SwitchCommands.register()
        MiscCommands.register()
    }
}

fun SubCommandBuilder.guild() {
    int("server-id", "The ID for the server") {
        required = false
    }
}
fun SubCommandBuilder.name(name: String = "name", required: Boolean = true) {
    string(name, "The $name to use") {
        this.required = required
    }
}
fun SubCommandBuilder.avatar(name: String = "avatar") {
    attachment(name, "The $name to set") {
        required = false
    }
}
fun SubCommandBuilder.bool(name: String, desc: String) {
    boolean(name, desc) {
        required = false
    }
}
fun SubCommandBuilder.raw() = bool("raw", "Whether to fetch the raw data")
fun SubCommandBuilder.clear() = bool("clear", "Whether to clear the data")
fun SubCommandBuilder.member() = name("member")
fun GlobalChatInputCreateBuilder.access(type: String, name: String, builder: SubCommandBuilder.() -> Unit) {
    subCommand(name, "Accesses the $type's $name", builder)
}