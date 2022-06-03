package dev.proxyfox.string.node

import dev.proxyfox.string.parser.MessageHolder

interface Node {
    fun parse(string: String, index: Int, holder: MessageHolder): Int
    fun getSubNodes(): Array<Node>
    fun addSubNode(node: Node)
    suspend fun execute(holder: MessageHolder): String
}