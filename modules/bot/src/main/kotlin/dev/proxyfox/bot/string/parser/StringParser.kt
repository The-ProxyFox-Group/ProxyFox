/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.parser

import dev.kord.core.entity.Message
import dev.proxyfox.bot.string.node.LiteralNode
import dev.proxyfox.bot.string.node.Node
import dev.proxyfox.common.logger

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
    // Check if returned index is greater than the string length or zero
    if (idx == 0) return null
    if (idx >= input.length) return execute(node, holder)
    // Get a substring with the index
    val subStr = input.substring(idx).trim()
    // Loop through sub nodes and try to execute
    for (subNode in node.getSubNodes()) {
        val str = tryExecuteNode(subStr, subNode, holder)
        if (str != null) return str
    }
    // Try to run the executor
    return execute(node, holder)
}

suspend fun execute(node: Node, holder: MessageHolder): String {
    return node.execute(holder)
}