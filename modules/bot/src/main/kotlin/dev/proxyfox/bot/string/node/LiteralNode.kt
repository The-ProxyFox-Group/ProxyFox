package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class LiteralNode(private val literals: Array<out String>, val executor: suspend MessageHolder.() -> String) : Node {
    private val literalNodes: ArrayList<LiteralNode> = ArrayList()
    private val stringNodes: ArrayList<StringNode> = ArrayList()
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
        when (node) {
            is LiteralNode -> literalNodes.add(node)
            is StringNode -> stringNodes.add(node)
            is GreedyNode -> greedyNodes.add(node)
            is StringListNode -> greedyNodes.add(node)
        }
    }

    override suspend fun execute(holder: MessageHolder) = holder.executor()
}