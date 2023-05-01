/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.rest.builder.interaction.*
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.context.InteractionCommandContext
import dev.proxyfox.command.CommandParser
import dev.proxyfox.common.applyAsync
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.group.GroupRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * General utilities relating to commands
 * @author Oliver
 * */

object Commands {
    val parser = CommandParser<Any, DiscordContext<Any>>()

    suspend operator fun invoke(action: suspend Commands.() -> Unit) {
        applyAsync(action)
    }

    suspend operator fun CommandRegistrar.unaryPlus() {
        printStep("Registering $displayName commands", 3)
        parser.registerTextCommands()
        registerSlashCommands()
    }
}

fun SubCommandBuilder.guild() {
    integer("server-id", "The ID for the server") {
        required = false
    }
}

fun SubCommandBuilder.name(name: String = "name", required: Boolean = true) {
    string(name, "The $name to use") {
        this.required = required
    }
}

fun SubCommandBuilder.enum(name: String, required: Boolean = true, enum: ArrayList<String>) {
    string(name, "The $name to use") {
        this.required = required
        for (value in enum) {
            choice(value, value)
        }
    }
}

fun SubCommandBuilder.system(name: String = "system") {
    string(name, "The $name to use") {
        required = false
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

@OptIn(ExperimentalContracts::class)
suspend fun <T> checkSystem(ctx: DiscordContext<T>, system: SystemRecord?, private: Boolean = false): Boolean {
    contract {
        returns(true) implies (system != null)
    }
    system ?: run {
        ctx.respondFailure(CommonMessages.NOT_FOUND("System"), private)
        return false
    }
    return true
}

@OptIn(ExperimentalContracts::class)
suspend fun <T> checkGroup(ctx: DiscordContext<T>, group: GroupRecord?, private: Boolean = false): Boolean {
    contract {
        returns(true) implies (group != null)
    }
    group ?: run {
        ctx.respondFailure(CommonMessages.NOT_FOUND("Group"), private)
        return false
    }
    return true
}

@OptIn(ExperimentalContracts::class)
suspend fun <T> checkMember(ctx: DiscordContext<T>, member: MemberRecord?, private: Boolean = false): Boolean {
    contract {
        returns(true) implies (member != null)
    }

    member ?: run {
        ctx.respondFailure(CommonMessages.NOT_FOUND("Member"), private)
        return false
    }
    return true
}

@OptIn(ExperimentalContracts::class)
suspend fun <T> checkSwitch(ctx: DiscordContext<T>, switch: SystemSwitchRecord?): Boolean {
    contract {
        returns(true) implies (switch != null)
    }

    switch ?: run {
        ctx.respondFailure(
            "Looks like you haven't registered any switches yet. Create one using `/switch create` or ${
                CommonMessages.TEXT_COMMAND(
                    "switch"
                )
            }"
        )
        return false
    }
    return true
}

suspend fun InteractionCommandContext.getSystem(): SystemRecord? {
    val id = value.interaction.command.strings["system"]
    return if (id == null)
        database.fetchSystemFromUser(getUser())
    else
        (database.fetchSystemFromId(id) ?: database.fetchSystemFromUser(id.toULongOrNull() ?: return null))?.let {
            if (!it.canAccess(getUser().id.value))
                return null
            return it
        }
}
