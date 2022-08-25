package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class GreedyNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override fun parse(string: String, index: Int, holder: MessageHolder): Int {
        if (index >= string.length) return index
        holder.params[name] = arrayOf(string.substring(index))
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder) = holder.executor()
}