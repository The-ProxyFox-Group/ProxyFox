package dev.proxyfox.bot.string.parser

import dev.kord.core.entity.Message
import dev.proxyfox.common.logger
import dev.proxyfox.bot.string.node.LiteralNode
import dev.proxyfox.bot.string.node.Node

val nodes: ArrayList<LiteralNode> = ArrayList()

fun registerCommand(node: LiteralNode) {
    nodes.add(node)
}

suspend fun parseString(input: String, message: Message): String? {
    for (node in nodes) {
        val str = tryExecuteNode(input, node, MessageHolder(message, HashMap()))
        if (str != null) return str
    }
    return null
}

suspend fun tryExecuteNode(input: String, node: Node, holder: MessageHolder): String? {
    // Parse out the command node
    val idx = node.parse(input, holder)
    // Check if returned index is greater than or less than the string length
    if (idx == 0) return null
    if (idx > input.length) return null
    // Get a substring with the index
    val subStr = input.substring(idx).trim()
    // Loop through sub nodes and try to execute
    for (subNode in node.getSubNodes()) {
        val str = tryExecuteNode(subStr, subNode, holder)
        if (str != null) return str
    }
    // Try to run the executor
    return try {
        node.execute(holder)
    } catch (err: Throwable) {
        // Catch any errors and log them
        val timestamp = System.currentTimeMillis()
        logger.warn(timestamp.toString())
        logger.warn(err.stackTraceToString())
        "An unexpected error occurred.\nTimestamp: `$timestamp`"
    }
}