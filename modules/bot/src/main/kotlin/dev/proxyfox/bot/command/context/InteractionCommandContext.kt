/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.context

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.*
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import dev.proxyfox.bot.command.menu.DiscordMenu
import dev.proxyfox.bot.command.menu.InteractionCommandMenu
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlin.jvm.optionals.getOrNull

class InteractionCommandContext(value: ChatInputCommandInteractionCreateEvent) :
    DiscordContext<ChatInputCommandInteractionCreateEvent>(value) {
    override val command: String = ""

    var deferred: DeferredMessageInteractionResponseBehavior? = null

    @OptIn(ExperimentalStdlibApi::class)
    override fun getAttachment(): Attachment? {
        return value.interaction.command.attachments.values.stream().findFirst().getOrNull()
    }

    override suspend fun respondPlain(text: String, private: Boolean): ChatInputCommandInteractionCreateEvent {
        if (deferred != null)
            deferred!!.respond {
                content = text
            }
        else if (private)
            value.interaction.respondEphemeral {
                content = text
            }
        else value.interaction.respondPublic {
            content = text
        }
        return value
    }

    override suspend fun getChannel(private: Boolean): MessageChannelBehavior {
        return if (private)
            value.interaction.user.getDmChannelOrNull()
                ?: value.interaction.channel
        else value.interaction.channel
    }

    override suspend fun getGuild(): Guild? {
        return (value.interaction.channel as? GuildChannelBehavior)?.getGuildOrNull()
    }

    override suspend fun getUser(): User {
        return value.interaction.user
    }

    override suspend fun getMember(): Member? {
        return getGuild()?.getMemberOrNull(getUser().id)
    }

    override suspend fun respondEmbed(
        private: Boolean,
        text: String?,
        embed: suspend EmbedBuilder.() -> Unit
    ): ChatInputCommandInteractionCreateEvent {
        if (deferred != null)
            deferred!!.respond {
                embed {
                    embed()
                }
            }
        else if (private)
            value.interaction.respondEphemeral {
                embed {
                    embed()
                }
            }
        else value.interaction.respondPublic {
            embed {
                embed()
            }
        }
        return value
    }

    override suspend fun deferResponse(private: Boolean) {
        deferred = if (private)
            value.interaction.deferEphemeralResponse()
        else value.interaction.deferPublicResponse()
    }

    override suspend fun tryDeleteTrigger(reason: String?) {
    }

    override suspend fun optionalSuccess(text: String): ChatInputCommandInteractionCreateEvent {
        respondSuccess(text, true)
        return value
    }

    override suspend fun getDatabaseMessage(
        system: SystemRecord?,
        messageId: Snowflake?
    ): Pair<Message?, ProxiedMessageRecord?> {
        val databaseMessage = if (messageId != null) {
            database.fetchMessage(messageId)
        } else if (system != null) {
            database.fetchLatestMessage(system.id, getChannel().id)
        } else null
        databaseMessage ?: return null to null
        val message = getChannel().getMessageOrNull(Snowflake(databaseMessage.newMessageId))
        return message to databaseMessage
    }

    override suspend fun interactionMenu(private: Boolean, action: suspend DiscordMenu.() -> Unit) {

        val message =
            deferred
                ?: if (private) value.interaction.deferEphemeralResponse() else value.interaction.deferPublicResponse()
        val menu = InteractionCommandMenu(message.respond {
            content = "Thinking..."
        }, value.interaction.user.id)
        menu.action()
        menu.init()
    }
}