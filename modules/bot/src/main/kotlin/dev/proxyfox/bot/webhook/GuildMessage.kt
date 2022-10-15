/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.GuildMessageChannelBehavior
import dev.kord.core.entity.*

// Created 2022-15-10T00:34:30

/**
 * @author Ampflower
 * @since ${version}
 **/
@JvmRecord
data class GuildMessage(
    val id: Snowflake,
    val content: String,
    val author: User,
    val channel: GuildMessageChannelBehavior,
    val guild: Guild,
    val attachments: Collection<Attachment>,
    val embeds: Collection<Embed>,
    val referencedMessage: Message?,
    val rawBehaviour: MessageBehavior,
) {
    constructor(
        message: Message,
        guild: Guild,
        channel: GuildMessageChannelBehavior,
        author: User = message.author!!
    ) : this(
        id = message.id,
        content = message.content,
        author = author,
        channel = channel,
        guild = guild,
        attachments = message.attachments,
        embeds = message.embeds,
        referencedMessage = message.referencedMessage,
        rawBehaviour = message,
    )
}
