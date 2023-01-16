/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.proxyfox.command.menu.CommandScreen

class DiscordScreen(name: String, private val menu: DiscordMenu) : CommandScreen(name) {
    private var initializer: suspend () -> Unit = {}

    fun onInit(action: suspend () -> Unit) {
        initializer = action
    }

    override suspend fun init() {
        initializer()
    }
}