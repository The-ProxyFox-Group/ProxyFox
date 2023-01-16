/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.context

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.*
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.command.CommandContext
import dev.proxyfox.common.applyAsync
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlin.jvm.optionals.getOrNull

class DiscordMessageContext(message: Message, override val command: String): DiscordContext<Message>(message) {
    @OptIn(ExperimentalStdlibApi::class)
    override fun getAttachment(): Attachment? {
        return value.attachments.stream().findFirst().getOrNull()
    }

    override suspend fun getChannel(private: Boolean): MessageChannelBehavior {
        return if (private)
            value.author?.getDmChannelOrNull()
                ?: value.channel
        else value.channel
    }

    override suspend fun getGuild(): Guild? {
        return value.getGuildOrNull()
    }

    override suspend fun getUser(): User? {
        return value.author
    }

    override suspend fun getMember(): Member? {
        return value.getAuthorAsMember()
    }

    override suspend fun respondEmbed(
        private: Boolean,
        text: String?,
        embed: suspend EmbedBuilder.() -> Unit
    ): Message {
        return getChannel(private).createMessage {
            content = text

            embeds.add(EmbedBuilder().applyAsync(embed))
        }
    }

    override suspend fun tryDeleteTrigger(reason: String?) {
        if (value.getGuildOrNull() != null) value.delete(reason)
    }

    override suspend fun optionalSuccess(text: String): Message {
        return value
    }

    override suspend fun respondPager() {
        TODO("Not yet implemented")
    }

    override suspend fun getDatabaseMessage(system: SystemRecord?, messageId: Snowflake?): Pair<Message?, ProxiedMessageRecord?> {
        val databaseMessage = if (messageId != null) {
            database.fetchMessage(messageId)
        } else if (value.referencedMessage != null) {
            database.fetchMessage(value.referencedMessage!!.id)
        } else if (system != null) {
            database.fetchLatestMessage(system.id, getChannel().id)
        } else null
        databaseMessage ?: return null to null
        val message = getChannel().getMessageOrNull(Snowflake(databaseMessage.newMessageId))
        return message to databaseMessage
    }

    override suspend fun respondPlain(text: String, private: Boolean): Message {
        return getChannel(private).createMessage(text)
    }

    override suspend fun respondSuccess(text: String, private: Boolean): Message {
        return getChannel(private).createMessage("✅ $text")
    }

    override suspend fun respondWarning(text: String, private: Boolean): Message {
        return getChannel(private).createMessage("⚠️ $text")
    }

    override suspend fun respondFailure(text: String, private: Boolean): Message {
        return getChannel(private).createMessage("❌ $text")
    }

    override suspend fun timedYesNoPrompt(
        text: String,
        yesAction: Pair<String, suspend CommandContext<Message>.() -> Boolean>,
        noAction: Pair<String, suspend CommandContext<Message>.() -> Boolean>,
        timeoutAction: suspend CommandContext<Message>.() -> Boolean,
        private: Boolean
    ) {
        // TODO: Move TimedYesNoPrompt here
    }

}