/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.response.EphemeralMessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord

class InteractionCommandMenu(val interaction: EphemeralMessageInteractionResponse) : DiscordMenu() {
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

    private suspend fun interact(button: ButtonInteractionCreateEvent) {
        if (button.interaction.message.id != interaction.message.id) return
        button.interaction.deferEphemeralMessageUpdate()
        active!!.click(button.interaction.componentId)
    }
}
