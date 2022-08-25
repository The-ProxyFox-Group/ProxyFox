package dev.proxyfox.bot.string.parser

import dev.kord.core.entity.Message

data class MessageHolder(
    val message: Message,
    val params: HashMap<String, Array<String>>
)