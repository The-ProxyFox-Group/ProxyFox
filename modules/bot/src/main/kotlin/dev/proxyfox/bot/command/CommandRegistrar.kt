/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.command.CommandParser

interface CommandRegistrar {
    val displayName: String

    suspend fun CommandParser<Any, DiscordContext<Any>>.registerTextCommands()
    suspend fun registerSlashCommands()
}