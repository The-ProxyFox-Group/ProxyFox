/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class StringNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override val type: NodeType = NodeType.VARIABLE

    private val literalNodes: ArrayList<Node> = ArrayList()
    private val stringNodes: ArrayList<Node> = ArrayList()
    private val greedyNodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        when (string[0]) {
            '"' -> {
                var out = ""
                for (i in string.substring(1).indices) {
                    if (string[i+1] == '"') {
                        holder.params[name] = arrayOf(out)
                        return i + 2
                    }
                    out += string[i+1].toString()
                }
                holder.params[name] = arrayOf(out)
            }

            '\'' -> {
                var out = ""
                for (i in string.substring(1).indices) {
                    if (string[i+1] == '\'') {
                        holder.params[name] = arrayOf(out)
                        return i + 2
                    }
                    out += string[i+1].toString()
                }
                holder.params[name] = arrayOf(out)
            }

            else -> {
                var out = ""
                for (i in string.indices) {
                    if (string[i] == ' ') {
                        holder.params[name] = arrayOf(out)
                        return i
                    }
                    out += string[i].toString()
                }
                holder.params[name] = arrayOf(out)
            }
        }
        return string.length
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

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}