package io.github.proxyfox.string.node

import io.github.proxyfox.string.parser.MessageHolder

class GreedyNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override fun parse(string: String, index: Int, holder: MessageHolder): Int {
        holder.params[name] = string.substring(index)
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder) = executor(holder)
}