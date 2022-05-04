package io.github.proxyfox.string.parser

import dev.kord.core.entity.Message
import io.github.proxyfox.string.node.LiteralNode
import io.github.proxyfox.string.node.Node

val nodes: ArrayList<LiteralNode> = ArrayList()

suspend fun parseString(input: String, message: Message): String? {
    for (node in nodes) {
        val str = tryExecuteNode(input, 0, node, MessageHolder(message, HashMap()))
        if (str != null) return str
    }
    return null
}

suspend fun tryExecuteNode(input: String, index: Int, node: Node, holder: MessageHolder): String? {
    val newIdx = node.parse(input, index, holder)
    if (newIdx == index) return null
    for (newNode in node.getSubNodes()) {
        val str = tryExecuteNode(input, newIdx, newNode, holder)
        if (str != null) return str
    }
    return node.execute(holder)
}