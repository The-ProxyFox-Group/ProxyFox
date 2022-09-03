package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

interface Node {
    fun parse(string: String, holder: MessageHolder): Int
    fun getSubNodes(): Array<Node>
    fun addSubNode(node: Node)
    suspend fun execute(holder: MessageHolder): String
}