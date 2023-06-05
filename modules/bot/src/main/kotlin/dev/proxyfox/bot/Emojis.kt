/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji

object Emojis {
    val check = "✅".partial
    val multiply = "✖".partial
    val wastebasket = "🗑".partial
    val move = "🔀".partial
    val rewind = "⏪".partial
    val fastforward = "⏩".partial
    val last = "⬅".partial
    val next = "➡".partial
    val numbers = "\uD83D\uDD22".partial

    val ReactionEmoji.Unicode.partial get() = name.partial

    val ReactionEmoji.Custom.partial get() = DiscordPartialEmoji(id = id, name = name, animated = isAnimated.optional())

    val GuildEmoji.partial get() = DiscordPartialEmoji(id = id, animated = isAnimated.optional())

    val String.partial get() = DiscordPartialEmoji(name = this)
}
