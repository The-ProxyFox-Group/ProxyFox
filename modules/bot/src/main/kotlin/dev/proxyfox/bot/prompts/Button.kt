/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.prompts

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.modify.MessageModifyBuilder

@JvmRecord
data class Button(
    val label: String? = "Confirm",
    val emoji: DiscordPartialEmoji? = null,
    val style: ButtonStyle = ButtonStyle.Secondary,
    val action: suspend MessageModifyBuilder.() -> Unit,
) {
    companion object {
        val check = DiscordPartialEmoji(name = "✅")
        val multiply = DiscordPartialEmoji(name = "✖")
        val wastebasket = DiscordPartialEmoji(name = "🗑")
        val move = DiscordPartialEmoji(name = "\uD83D\uDD00")

        val ReactionEmoji.Unicode.partial get() = DiscordPartialEmoji(name = name)

        val ReactionEmoji.Custom.partial get() = DiscordPartialEmoji(id = id, name = name, animated = isAnimated.optional())

        val GuildEmoji.partial get() = DiscordPartialEmoji(id = id, animated = isAnimated.optional())
    }
}