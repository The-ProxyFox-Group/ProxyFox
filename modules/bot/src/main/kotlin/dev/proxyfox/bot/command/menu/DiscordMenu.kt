/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.command.menu.CommandMenu
import dev.proxyfox.command.menu.CommandScreen
import kotlinx.coroutines.Job

abstract class DiscordMenu : CommandMenu() {
    internal val jobs = arrayListOf<Job>()

    var closed = false

    override suspend fun close() {
        jobs.forEach {
            it.cancel()
        }
        closed = true
    }

    override suspend fun createScreen(name: String): CommandScreen {
        return DiscordScreen(name)
    }

    abstract suspend fun edit(builder: suspend MessageModifyBuilder.() -> Unit)
}
