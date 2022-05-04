package io.github.proxyfox.string.node

import io.github.proxyfox.string.parser.MessageHolder

interface Node {
    suspend fun parse(string: String, index: Int, holder: MessageHolder): Int
    suspend fun getSubNodes(): Array<Node>
    suspend fun addSubNode(node: Node)
    suspend fun execute(holder: MessageHolder): String
}