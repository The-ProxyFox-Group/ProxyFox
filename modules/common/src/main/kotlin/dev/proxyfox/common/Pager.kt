/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.common

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.updateEphemeralMessage
import dev.kord.core.behavior.interaction.updatePublicMessage
import dev.kord.core.behavior.reply
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import java.util.*
import kotlin.math.max
import kotlin.math.min

// Created 2022-15-10T08:37:40

/**
 * @author KJP12
 * @since ${version}
 **/
class Pager<T>(
    private val runner: Snowflake,
    private val reference: Message,
    private val list: List<T>,
    private val pageSize: Int,
    private val pages: Int = ceilDiv(list.size, pageSize),
    kord: Kord,
    private val embed: suspend EmbedBuilder.(String) -> Unit,
    private val transform: suspend (T) -> String,
) {
    private var page = 0
    private var selector: ActiveSelector? = null

    private val interactionJob = kord.on(consumer = this::onInteraction)
    private val reactionJob = kord.on(consumer = this::onReaction)

    private suspend fun onInteraction(event: ButtonInteractionCreateEvent) = event.run {
        val message = interaction.message
        val user = interaction.user
        if (user.id == runner && message == this@Pager.reference) {
            when (interaction.componentId) {
                "skipToFirst" -> page(0)
                "back" -> page(page - 1)
                "next" -> page(page + 1)
                "skipToLast" -> page(pages - 1)
                "selection" -> {
                    val uuid = UUID.randomUUID().toString()
                    selector = EphemeralSelector(this@Pager, interaction.respondEphemeral {
                        content = "Which page would you like to go to?"
                        components += ActionRowBuilder().apply {
                            selectMenu(uuid) {
                                val opts = pages / 25
                                options += if (opts != 0) {
                                    (0..24).asSequence().map { (it * opts).toString() }.map { SelectOptionBuilder(it, it) }
                                } else {
                                    (1..pages).asSequence().map(Int::toString).map { SelectOptionBuilder(it, it) }
                                }
                                placeholder = (page + 1).toString()
                            }
                        }
                    }, uuid)
                }

                "close" -> {
                    // Kord's updatePublicMessage implementation is broken, see kordlib/kord#701
                    interaction.deferPublicMessageUpdate()
                    close()
                }
            }
        }
    }

    private suspend fun onReaction(event: ReactionAddEvent) = event.run {
        if (userId == runner && messageId == reference.id && channelId == reference.channelId) {
            when (emoji.name) {
                "⏪" -> page(0)
                // It's the same emoji twice for both branches,
                // just the other has `\uFEOF` at the end.
                "⬅", "⬅\uFE0F" -> page(page - 1)
                "➡", "➡\uFE0F" -> page(page + 1)
                "⏩" -> page(pages - 1)
                "\uD83D\uDD22" -> {
                    selector = LegacySelector(this@Pager, reference.reply {
                        content = "Which page would you like to go to?"
                        components += ActionRowBuilder().apply {
                            selectMenu("menu") {
                                val opts = pages / 25
                                options += if (opts != 0) {
                                    (0..24).asSequence().map { (it * opts + 1).toString() }.map { SelectOptionBuilder(it, it) }
                                } else {
                                    (1..pages).asSequence().map(Int::toString).map { SelectOptionBuilder(it, it) }
                                }
                                placeholder = (page + 1).toString()
                            }
                        }
                        components += cancel
                    })
                }

                "❌", "✖️" -> {
                    close()
                }
            }
            if (guildId != null) message.deleteReaction(userId, emoji)
        }
    }

    private suspend fun ButtonInteractionCreateEvent.page(inPage: Int) {
        // Kord's updatePublicMessage implementation is broken, see kordlib/kord#701
        interaction.deferPublicMessageUpdate()
        this@Pager.page(inPage)
    }

    private suspend fun page(inPage: Int) {
        page = min(max(0, inPage), pages - 1)
        reference.edit {
            embed {
                embed("${page + 1} / $pages")
                description = buildString(list, page, pageSize, transform)
            }
        }
    }

    private suspend fun close() {
        interactionJob.cancel()
        reactionJob.cancel()
        selector?.close()
        reference.edit { components = mutableListOf() }
    }

    private interface ActiveSelector {
        suspend fun close()
    }

    private class EphemeralSelector(
        val pager: Pager<*>,
        val message: EphemeralMessageInteractionResponseBehavior,
        val `discord is a massive pain by not returning the message that would eliminate the need for this`: String,
    ) : ActiveSelector {
        private val ephemeralListener = message.kord.on<SelectMenuInteractionCreateEvent> {
            if (interaction.componentId == `discord is a massive pain by not returning the message that would eliminate the need for this`) {
                pager.page(interaction.values[0].toInt() - 1)
                interaction.updateEphemeralMessage { content = "Pager changed to page ${pager.page + 1}." }
                closeInternal()
            }
        }

        private val messageListener = message.kord.on<MessageCreateEvent> {
            if (pager.runner == message.author?.id && pager.reference.channel == message.channel) {
                val page = message.content.toIntOrNull() ?: return@on
                pager.page(page - 1)
                message.delete("Intercepted by pager.")
                this@EphemeralSelector.message.edit {
                    content = "Pager changed to page ${pager.page + 1}."
                    components = mutableListOf()
                }
                closeInternal()
            }
        }

        override suspend fun close() {
            message.edit {
                content = "Pager closed."
                components = mutableListOf()
            }
            ephemeralListener.cancel()
        }

        fun closeInternal() {
            ephemeralListener.cancel()
            messageListener.cancel()
            pager.selector = null
        }
    }

    private class LegacySelector(
        val pager: Pager<*>,
        val message: MessageBehavior,
    ) : ActiveSelector {
        private val primaryListener = message.kord.on<SelectMenuInteractionCreateEvent> {
            val message = interaction.message
            val user = interaction.user
            if (user.id == pager.runner && message == this@LegacySelector.message) {
                pager.page(interaction.values[0].toInt() - 1)
                interaction.updatePublicMessage {
                    content = "Pager changed to page ${pager.page + 1}."
                    components += freeDelete
                }
                closeInternal()
            }
        }

        private val messageListener = message.kord.on<MessageCreateEvent> {
            if (pager.runner == message.author?.id && pager.reference.channel == message.channel) {
                val page = message.content.toIntOrNull() ?: return@on
                pager.page(page - 1)
                message.delete("Intercepted by pager.")
                this@LegacySelector.message.edit {
                    content = "Pager changed to page ${pager.page + 1}."
                    components = mutableListOf(freeDelete)
                }
                closeInternal()
            }
        }

        override suspend fun close() {
            message.delete()
            closeInternal()
        }

        fun closeInternal() {
            primaryListener.cancel()
            messageListener.cancel()
            pager.selector = null
        }
    }

    companion object {
        private val cancel = ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Secondary, "cancel") {
                emoji(ReactionEmoji.Unicode("✖"))
                label = "Cancel"
            }
        }

        private val freeDelete = ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Secondary, "free-delete") {
                emoji(ReactionEmoji.Unicode("✖"))
                label = "Delete message"
            }
        }

        suspend fun <T> build(
            runner: Snowflake,
            channel: MessageChannelBehavior,
            list: List<T>,
            pageSize: Int,
            kord: Kord,
            embed: suspend EmbedBuilder.(String) -> Unit,
            transform: suspend (T) -> String,
        ): Pager<T> {
            val pages = ceilDiv(list.size, pageSize)
            val message = channel.createMessage {
                embed {
                    embed("1 / $pages")
                    description = buildString(list, 0, pageSize, transform)
                }
                components += ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Primary, "skipToFirst") { emoji(ReactionEmoji.Unicode("⏪")) }
                    interactionButton(ButtonStyle.Primary, "back") { emoji(ReactionEmoji.Unicode("⬅")) }
                    interactionButton(ButtonStyle.Primary, "next") { emoji(ReactionEmoji.Unicode("➡")) }
                    interactionButton(ButtonStyle.Primary, "skipToLast") { emoji(ReactionEmoji.Unicode("⏩")) }
                }
                components += ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Secondary, "selection") {
                        emoji(ReactionEmoji.Unicode("\uD83D\uDD22"))
                        label = "Select Page"
                    }
                    interactionButton(ButtonStyle.Danger, "close") {
                        // :heavy_multiplication_x: is used instead of :x:
                        // due to providing better contrast in almost every case.
                        emoji(ReactionEmoji.Unicode("✖️"))
                        label = "Close"
                    }
                }
            }
            return Pager(runner, message, list, pageSize, pages, kord, embed, transform)
        }

        private suspend fun <T> buildString(
            list: List<T>,
            page: Int,
            size: Int,
            transform: suspend (T) -> String,
        ) = buildString {
            for (i in page * size until min(page * size + size, list.size)) {
                append(transform(list[i]))
            }
        }
    }
}