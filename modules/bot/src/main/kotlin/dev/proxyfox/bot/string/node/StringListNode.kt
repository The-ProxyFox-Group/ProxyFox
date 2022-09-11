/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class StringListNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override val type: NodeType = NodeType.GREEDY

    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        var i = 0
        val arr = ArrayList<String>()
        while (i < string.length) {
            when (string[i]) {
                '"' -> {
                    var out = ""
                    for (j in string.substring(1).indices) {
                        if (string[j] == '"') {
                            arr.add(out)
                            i += j + 1
                            continue
                        }
                        out += string[j].toString()
                    }
                    arr.add(out)
                }

                '\'' -> {
                    var out = ""
                    for (j in string.substring(1).indices) {
                        if (string[j] == '\'') {
                            arr.add(out)
                            i += j + 1
                            continue
                        }
                        out += string[j].toString()
                    }
                    arr.add(out)
                }

                else -> {
                    var out = ""
                    for (j in string.indices) {
                        if (string[j] == ' ') {
                            arr.add(out)
                            i += j
                            continue
                        }
                        out += string[j].toString()
                    }
                    arr.add(out)
                }
            }
            i = string.length
        }
        holder.params[name] = arr.toTypedArray()
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}