/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.proxyfox.bot.string.node.Node
import dev.proxyfox.common.printStep

/**
 * General utilities relating to commands
 * @author Oliver
 * */

typealias CommandNode = suspend Node.() -> Unit

object Commands {
    suspend fun register() {
        printStep("Registering commands",1)
        SystemCommands.register()
        MemberCommands.register()
        SwitchCommands.register()
        MiscCommands.register()
    }
}