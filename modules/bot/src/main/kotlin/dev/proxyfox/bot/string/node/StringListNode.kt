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
            if (string[i] == ' ') {
                i++
                continue
            }
            when (string[i]) {
                '"' -> {
                    var out = ""
                    val substr = string.substring(i + 1)
                    for (j in substr.indices) {
                        if (substr[j] == '"')
                            break
                        out += substr[j].toString()
                    }
                    if (out.isNotEmpty()) {
                        arr.add(out)
                        i += out.length + 2
                        continue
                    }
                    if (out.isNotEmpty()) arr.add(out)
                }

                '\'' -> {
                    var out = ""
                    val substr = string.substring(i + 1)
                    for (j in substr.indices) {
                        if (substr[j] == '\'')
                            break
                        out += substr[j].toString()
                    }
                    if (out.isNotEmpty()) {
                        arr.add(out)
                        i += out.length + 2
                        continue
                    }
                }

                else -> {
                    var out = ""
                    val substr = string.substring(i)
                    for (j in substr.indices) {
                        if (substr[j] == ' ')
                            break
                        out += substr[j].toString()
                    }
                    if (out.isNotEmpty()) {
                        arr.add(out)
                        i += out.length
                        continue
                    }
                }
            }
            i++
        }
//        for (s in arr) {
//            logger.info(s)
//        }
        holder.params[name] = arr.toTypedArray()
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}