/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

enum class CommonMessages(val builder: (Array<out String>) -> String) {
    TEXT_COMMAND({
        "`${dev.proxyfox.bot.prefix}>${it[0]}`"
    }),
    NOT_FOUND({
        "${it[0]} not found. Create one using `/system create` or ${TEXT_COMMAND("system new")}"
    }),
    NOT_FOUND_WITH_NAME({
        "${it[0]} ${it[1]} not found. Create one using `/member create` or ${TEXT_COMMAND("member new")}"
    })
}

operator fun CommonMessages.invoke(vararg strings: String): String = builder(strings)
