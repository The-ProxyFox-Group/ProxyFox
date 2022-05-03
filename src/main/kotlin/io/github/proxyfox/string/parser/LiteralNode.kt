package io.github.proxyfox.string.parser

class LiteralNode(val literal: String, val executor: suspend (MessageHolder) -> String) : Node {
    val nodes: ArrayList<Node> = ArrayList()

    override suspend fun parse(string: String, index: Int, holder: MessageHolder): Int {
        if (string.substring(index, literal.length).lowercase() == literal.lowercase()) {
            return index + literal.length
        }
        return index
    }

    override suspend fun getSubNodes(): List<Node> = nodes

    override suspend fun addSubNode(node: Node) {
        nodes.add(node)
    }

    override suspend fun execute(holder: MessageHolder) = executor(holder)
}