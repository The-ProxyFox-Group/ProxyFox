/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.parser

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.bot.NagType
import dev.proxyfox.bot.shouldNag
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

        val nag = shouldNag(message.author, channel, msg)

        return channel.createMessage {
            if (msg.isNotBlank()) {
                content = if (nag != NagType.NONE) "$msg\n---\n⚠️ ${nag.message}" else msg
            }

            if (embed != null) {
                // TODO: an `embedAsync` helper function
                embeds.add(EmbedBuilder().applyAsync(embed))
                if (nag != NagType.NONE) {
                    embeds.add(EmbedBuilder().apply {
                        color = Color(0xFFFF77)
                        title = "⚠️ ProxyFox is shutting down"
                        description = nag.message
                    })
                }
            }

            if (nag != NagType.NONE) {
                this.components.add(ActionRowBuilder().apply {
                    this.interactionButton(ButtonStyle.Secondary, "export") {
                        label = "Export"
                        emoji(ReactionEmoji.Unicode("\uD83D\uDCE4"))
                    }
                    this.linkButton("https://proxyfox.dev") {
                        label = "Read More"
                    }
                })
            }
        }
    }

    suspend fun sendFiles(vararg files: NamedFile) {
        message.author!!.getDmChannel().createMessage {
            this.files.addAll(files)
        }
    }

    suspend fun hasRequired(permission: Permission): Boolean {
        val author = message.getAuthorAsMember() ?: return false
        return author.getPermissions().contains(permission)
    }
}