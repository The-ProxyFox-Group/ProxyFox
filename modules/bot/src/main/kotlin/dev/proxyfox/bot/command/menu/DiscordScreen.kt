/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.proxyfox.command.menu.CommandScreen

typealias SelectAction = suspend (List<String>) -> Unit

class DiscordScreen(name: String) : CommandScreen(name) {
    private var initializer: suspend () -> Unit = {}

    var selects = HashMap<String, SelectAction>()

    fun onInit(action: suspend () -> Unit) {
        initializer = action
    }

    fun select(name: String, action: SelectAction) {
        selects[name] = action
    }

    override suspend fun init() {
        initializer()
    }
}
