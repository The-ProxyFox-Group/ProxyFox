package dev.proxyfox.bot.command.context

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.*
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.command.CommandContext
import dev.proxyfox.common.applyAsync
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

    override suspend fun respondFiles(text: String?, vararg files: NamedFile): Message {
        return getChannel(true).createMessage {
            this.files.addAll(files)
        }
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