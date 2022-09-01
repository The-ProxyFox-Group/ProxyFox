package dev.proxyfox.bot.string.parser

import dev.kord.core.entity.Embed
import dev.kord.core.entity.Message
import dev.kord.core.behavior.channel.*
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed

data class MessageHolder(
    val message: Message,
    val params: HashMap<String, Array<String>>
) {
    // TODO: Check if can send in channels
    suspend fun respond(msg: String, dm: Boolean, embed: (EmbedBuilder.() -> Unit)? = null) {
        val channel = if (dm)
            message.author?.getDmChannelOrNull()
                ?: message.channel
        else message.channel

        channel.createMessage {
            if (msg.isNotBlank()) content = msg
            if (embed != null) embed(embed)
        }
    }
}