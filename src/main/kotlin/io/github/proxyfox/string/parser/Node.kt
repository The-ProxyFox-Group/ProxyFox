package io.github.proxyfox.string.parser

interface Node {
    suspend fun parse(string: String, index: Int, holder: MessageHolder): Int
    suspend fun getSubNodes(): List<Node>
    suspend fun addSubNode(node: Node)
    suspend fun execute(holder: MessageHolder): String
}