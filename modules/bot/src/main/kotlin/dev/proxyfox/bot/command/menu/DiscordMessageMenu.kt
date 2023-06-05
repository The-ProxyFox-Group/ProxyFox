/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.menu

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.common.onlyIf

class DiscordMessageMenu(val message: Message, val userId: Snowflake) : DiscordMenu() {
    override suspend fun edit(builder: suspend MessageModifyBuilder.() -> Unit) {
        message.edit {
            builder()
        }
    }

    override suspend fun init() {
        jobs.addAll(
            arrayListOf(
                kord.onlyIf<ButtonInteractionCreateEvent>({ interaction.message.id }, message.id) {
                    buttonInteract(this)
                },
                kord.onlyIf<SelectMenuInteractionCreateEvent>({ interaction.message.id }, message.id) {

                }
            )
        )
        super.init()
    }

    private suspend fun buttonInteract(button: ButtonInteractionCreateEvent) {
        if (button.interaction.user.id != userId) return
        button.interaction.deferPublicMessageUpdate()
        active!!.click(button.interaction.componentId)
    }

    private suspend fun selectInteract(select: SelectMenuInteractionCreateEvent) {
        if (select.interaction.user.id != userId) return
        select.interaction.deferPublicMessageUpdate()
        (active!! as DiscordScreen).selects[select.interaction.componentId]?.let { it(select.interaction.values) }
    }
}
