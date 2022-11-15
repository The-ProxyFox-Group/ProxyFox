/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.context

import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.command.CommandContext
import kotlin.jvm.optionals.getOrNull

class InteractionCommandContext(value: ChatInputCommandInteractionCreateEvent) :
    DiscordContext<ChatInputCommandInteractionCreateEvent>(value) {
    override val command: String = ""

    @OptIn(ExperimentalStdlibApi::class)
    override fun getAttachment(): Attachment? {
        return value.interaction.command.attachments.values.stream().findFirst().getOrNull()
    }

    override suspend fun respondFailure(text: String, private: Boolean): ChatInputCommandInteractionCreateEvent {
        if (private)
            value.interaction.respondEphemeral {
                content = "❌ $text"
            }
        else value.interaction.respondPublic {
            content = "❌️ $text"
        }
        return value
    }

    override suspend fun respondPlain(text: String, private: Boolean): ChatInputCommandInteractionCreateEvent {
        if (private)
            value.interaction.respondEphemeral {
                content = text
            }
        else value.interaction.respondPublic {
            content = text
        }
        return value
    }

    override suspend fun respondSuccess(text: String, private: Boolean): ChatInputCommandInteractionCreateEvent {
        if (private)
            value.interaction.respondEphemeral {
                content = "✅ $text"
            }
        else value.interaction.respondPublic {
            content = "✅️ $text"
        }
        return value
    }

    override suspend fun respondWarning(text: String, private: Boolean): ChatInputCommandInteractionCreateEvent {
        if (private)
            value.interaction.respondEphemeral {
                content = "⚠️ $text"
            }
        else value.interaction.respondPublic {
            content = "⚠️ $text"
        }
        return value
    }

    override suspend fun timedYesNoPrompt(
        text: String,
        yesAction: Pair<String, suspend CommandContext<ChatInputCommandInteractionCreateEvent>.() -> Boolean>,
        noAction: Pair<String, suspend CommandContext<ChatInputCommandInteractionCreateEvent>.() -> Boolean>,
        timeoutAction: suspend CommandContext<ChatInputCommandInteractionCreateEvent>.() -> Boolean,
        private: Boolean
    ) {
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

    override suspend fun respondFiles(text: String?, vararg files: NamedFile): ChatInputCommandInteractionCreateEvent {
        getChannel(true).createMessage {
            this.files.addAll(files)
        }
        return value
    }

    override suspend fun respondEmbed(
        private: Boolean,
        text: String?,
        embed: suspend EmbedBuilder.() -> Unit
    ): ChatInputCommandInteractionCreateEvent {
        if (private)
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

    override suspend fun tryDeleteTrigger(reason: String?) {

    }
}