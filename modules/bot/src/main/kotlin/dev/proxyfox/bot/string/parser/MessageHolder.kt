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
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.common.applyAsync

data class MessageHolder(
    val message: Message,
    val params: HashMap<String, Array<String>>
) {
    // TODO: Check if can send in channels
    suspend fun respond(msg: String = "", dm: Boolean = false, embed: (suspend EmbedBuilder.() -> Unit)? = null): Message {
        val channel = if (dm)
            message.author?.getDmChannelOrNull()
                ?: message.channel
        else message.channel

        return channel.createMessage {
            if (msg.isNotBlank()) content = msg
            // TODO: an `embedAsync` helper function
            if (embed != null) embeds.add(EmbedBuilder().applyAsync(embed))
        }
    }
    suspend fun sendFiles(vararg files: NamedFile) {
        message.author!!.getDmChannel().createMessage {
            this.files.addAll(files)
        }
    }
}