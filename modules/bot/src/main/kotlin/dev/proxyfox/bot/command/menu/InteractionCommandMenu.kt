/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.response.EphemeralMessageInteractionResponse
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.common.onlyIf

class InteractionCommandMenu(val interaction: MessageInteractionResponse, val userId: Snowflake) : DiscordMenu() {
    override suspend fun edit(builder: suspend MessageModifyBuilder.() -> Unit) {
        interaction.edit {
            builder()
        }
    }

    override suspend fun init() {
        jobs.addAll(
            arrayListOf(
                kord.onlyIf<ButtonInteractionCreateEvent>({ interaction.message.id }, interaction.message.id) {
                    buttonInteract(this)
                },
                kord.onlyIf<SelectMenuInteractionCreateEvent>({ interaction.message.id }, interaction.message.id) {
                    selectInteract(this)
                }
            )
        )
        super.init()
    }


    private suspend fun buttonInteract(button: ButtonInteractionCreateEvent) {
        if (interaction is EphemeralMessageInteractionResponse)
            button.interaction.deferEphemeralMessageUpdate()
        else button.interaction.deferPublicMessageUpdate()
        active!!.click(button.interaction.componentId)
    }

    private suspend fun selectInteract(select: SelectMenuInteractionCreateEvent) {
        if (interaction is EphemeralMessageInteractionResponse)
            select.interaction.deferEphemeralMessageUpdate()
        else select.interaction.deferPublicMessageUpdate()
        (active!! as DiscordScreen).selects[select.interaction.componentId]?.let { it(select.interaction.values) }
    }
}
