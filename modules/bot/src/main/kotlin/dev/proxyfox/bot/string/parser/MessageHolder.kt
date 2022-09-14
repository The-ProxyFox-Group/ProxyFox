/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.parser

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed

data class MessageHolder(
    val message: Message,
    val params: HashMap<String, Array<String>>
) {
    // TODO: Check if can send in channels
    suspend fun respond(msg: String, dm: Boolean = false, embed: (EmbedBuilder.() -> Unit)? = null) {
        val channel = if (dm)
            message.author?.getDmChannelOrNull()
                ?: message.channel
        else message.channel

        channel.createMessage {
            if (msg.isNotBlank()) content = msg
            if (embed != null) embed(embed)
        }
    }
}