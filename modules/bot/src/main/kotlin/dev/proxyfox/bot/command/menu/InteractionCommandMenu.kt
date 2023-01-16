/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord

class InteractionCommandMenu(val interaction: EphemeralMessageInteractionResponseBehavior) : DiscordMenu() {
    override suspend fun edit(builder: MessageModifyBuilder.() -> Unit) {
        interaction.edit(builder)
    }

    override suspend fun init() {
        jobs.add(
            kord.on<ButtonInteractionCreateEvent> {
                interact(this)
            }
        )
        super.init()
    }

    private fun interact(button: ButtonInteractionCreateEvent) {
        TODO()
    }
}