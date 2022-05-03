package io.github.proxyfox.string.parser

class LiteralNode(val literal: String) : Node {
    val nodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, index: Int): Int {
        if (string.substring(index, literal.length).lowercase() == literal.lowercase()) {
            return index + literal.length
        }
        return index
    }

    override fun getSubNodes(): List<Node> = nodes

    override fun addSubNode(node: Node) {
        nodes.add(node)
    }

    override fun execute(holder: MessageHolder) {
        TODO("Not yet implemented")
    }
}