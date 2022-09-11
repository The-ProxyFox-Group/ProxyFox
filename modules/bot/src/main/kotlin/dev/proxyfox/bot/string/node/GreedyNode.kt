/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class GreedyNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override val type: NodeType = NodeType.GREEDY

    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        holder.params[name] = arrayOf(string)
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder) = holder.executor()
}