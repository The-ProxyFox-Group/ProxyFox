/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class LiteralNode(private val literals: Array<out String>, val executor: suspend MessageHolder.() -> String) : Node {
    override val type: NodeType = NodeType.LITERAL

    private val literalNodes: ArrayList<Node> = ArrayList()
    private val stringNodes: ArrayList<Node> = ArrayList()
    private val greedyNodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, holder: MessageHolder): Int {
        for (literal in literals) {
            if (string.length < literal.length) continue
            if (string.length == literal.length && string.lowercase() == literal.lowercase())
                return literal.length
            if (string.lowercase().startsWith(literal.lowercase() + " "))
                return literal.length
        }
        return 0
    }

    override fun getSubNodes(): Array<Node> {
        val literalArray: Array<Node> = literalNodes.toTypedArray()
        val stringArray: Array<Node> = stringNodes.toTypedArray()
        val greedyArray: Array<Node> = greedyNodes.toTypedArray()
        return literalArray + stringArray + greedyArray
    }

    override fun addSubNode(node: Node) {
        when (node.type) {
            NodeType.LITERAL -> literalNodes.add(node)
            NodeType.VARIABLE -> stringNodes.add(node)
            NodeType.GREEDY -> greedyNodes.add(node)
        }
    }

    override suspend fun execute(holder: MessageHolder) = holder.executor()
}