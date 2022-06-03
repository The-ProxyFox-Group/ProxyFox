package dev.proxyfox.string.parser

import dev.kord.core.entity.Message

data class MessageHolder(
    val message: Message,
    val params: HashMap<String, String>
)