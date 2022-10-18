/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.prompts

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.bot.prompts.Button.Companion.check
import dev.proxyfox.bot.prompts.Button.Companion.multiply
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * @author Ampflower
 * @since ${version}
 **/
class TimedYesNoPrompt(
    runner: Snowflake,
    reference: Message,
    timeout: Duration = 1.minutes,
    private val yes: suspend MessageModifyBuilder.() -> Unit,
    private val no: suspend MessageModifyBuilder.() -> Unit,
) : TimedPrompt(
    runner,
    reference,
    timeout
) {
    init {
        jobs = listOf(
            kord.on(consumer = this::onInteraction),
            kord.on(consumer = this::onReaction),
        )
    }

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

    override suspend fun close() {
        reference.edit { no(); components = mutableListOf() }
        closeInternal()
    }

    companion object {
        suspend fun build(
            runner: Snowflake,
            channel: MessageChannelBehavior,
            timeout: Duration = 1.minutes,
            message: String,
            yes: Pair<String, suspend MessageModifyBuilder.() -> Unit>,
            no: Pair<String, suspend MessageModifyBuilder.() -> Unit> = "Cancel" to { content = "Action cancelled." },
        ): TimedYesNoPrompt {
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
            return TimedYesNoPrompt(
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
        ): TimedYesNoPrompt {
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
            return TimedYesNoPrompt(
                runner,
                msg,
                timeout,
                yes.action,
                no.action,
            )
        }
    }
}