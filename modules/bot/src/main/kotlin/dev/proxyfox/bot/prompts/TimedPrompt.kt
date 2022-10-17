/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.prompts

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.bot.scope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * @author Ampflower
 * @since ${version}
 **/
class TimedPrompt(
    private val runner: Snowflake,
    private val reference: Message,
    timeout: Duration = 1.minutes,
    private val yes: suspend MessageModifyBuilder.() -> Unit,
    private val no: suspend MessageModifyBuilder.() -> Unit,
) {
    private val timerJob = scope.launch {
        delay(timeout)
        close()
    }
    private val interactionJob = kord.on(consumer = this::onInteraction)
    private val reactionJob = kord.on(consumer = this::onReaction)

    private suspend fun onInteraction(event: ButtonInteractionCreateEvent) = event.run {
        if (interaction.message == reference && interaction.user.id == runner) {
            when (interaction.componentId) {
                "yes" -> {
                    interaction.deferPublicMessageUpdate().edit { yes(); components = mutableListOf() }
                    closeInternal()
                }

                "no" -> {
                    interaction.deferPublicMessageUpdate().edit { no(); components = mutableListOf() }
                    closeInternal()
                }
            }
        }
    }

    private suspend fun onReaction(event: ReactionAddEvent) = event.run {
        if (message == reference && userId == runner) {
            when (emoji.name) {
                "✅" -> {
                    reference.edit {
                        yes()
                        components = mutableListOf()
                    }
                    closeInternal()
                }

                "❌" -> {
                    reference.edit { no(); components = mutableListOf() }
                    closeInternal()
                }
            }
        }
    }

    suspend fun close() {
        reference.edit { no(); components = mutableListOf() }
        closeInternal()
    }

    fun closeInternal() {
        interactionJob.cancel()
        reactionJob.cancel()
        timerJob.cancel()
    }

    @JvmRecord
    data class Button(
        val label: String? = "Confirm",
        val emoji: DiscordPartialEmoji? = null,
        val style: ButtonStyle = ButtonStyle.Secondary,
        val action: suspend MessageModifyBuilder.() -> Unit,
    ) {
        companion object {
            val ReactionEmoji.Unicode.partial get() = DiscordPartialEmoji(name = name)

            val ReactionEmoji.Custom.partial get() = DiscordPartialEmoji(id = id, name = name, animated = isAnimated.optional())

            val GuildEmoji.partial get() = DiscordPartialEmoji(id = id, animated = isAnimated.optional())
        }
    }

    companion object {
        val check = DiscordPartialEmoji(name = "✅")
        val multiply = DiscordPartialEmoji(name = "✖")
        val wastebasket = DiscordPartialEmoji(name = "🗑")
        val move = DiscordPartialEmoji(name = "\uD83D\uDD00")

        suspend fun build(
            runner: Snowflake,
            channel: MessageChannelBehavior,
            timeout: Duration = 1.minutes,
            message: String,
            yes: Pair<String, suspend MessageModifyBuilder.() -> Unit>,
            no: Pair<String, suspend MessageModifyBuilder.() -> Unit> = "Cancel" to { content = "Action cancelled." },
        ): TimedPrompt {
            val msg = channel.createMessage {
                content = message
                components += ActionRowBuilder().apply {
                    interactionButton(ButtonStyle.Primary, "yes") {
                        emoji = check
                        label = yes.first
                    }
                    interactionButton(ButtonStyle.Secondary, "no") {
                        emoji = multiply
                        label = no.first
                    }
                }
            }
            return TimedPrompt(
                runner,
                msg,
                timeout,
                yes.second,
                no.second,
            )
        }

        suspend fun build(
            runner: Snowflake,
            channel: MessageChannelBehavior,
            timeout: Duration = 1.minutes,
            message: String,
            yes: Button,
            no: Button = Button("Cancel", multiply) { content = "Action cancelled." },
        ): TimedPrompt {
            val msg = channel.createMessage {
                content = message
                components += ActionRowBuilder().apply {
                    interactionButton(yes.style, "yes") {
                        emoji = yes.emoji
                        label = yes.label
                    }
                    interactionButton(no.style, "no") {
                        emoji = no.emoji
                        label = no.label
                    }
                }
            }
            return TimedPrompt(
                runner,
                msg,
                timeout,
                yes.action,
                no.action,
            )
        }
    }
}