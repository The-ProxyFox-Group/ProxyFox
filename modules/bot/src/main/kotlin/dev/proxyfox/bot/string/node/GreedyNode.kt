package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class GreedyNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        holder.params[name] = arrayOf(string)
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder) = holder.executor()
}