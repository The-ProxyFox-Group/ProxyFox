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
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.common.onlyIf

class InteractionCommandMenu(val interaction: MessageInteractionResponse) : DiscordMenu() {
    override suspend fun edit(builder: suspend MessageModifyBuilder.() -> Unit) {
        interaction.edit {
            builder()
        }
    }

    override suspend fun init() {
        jobs.add(
            kord.onlyIf<ButtonInteractionCreateEvent>({ interaction.message.id }, interaction.message.id) {
                interact(this)
            }
        )
        super.init()
    }


    private suspend fun interact(button: ButtonInteractionCreateEvent) {
        if (interaction is EphemeralMessageInteractionResponse)
            button.interaction.deferEphemeralMessageUpdate()
        else button.interaction.deferPublicMessageUpdate()
        active!!.click(button.interaction.componentId)
    }
}