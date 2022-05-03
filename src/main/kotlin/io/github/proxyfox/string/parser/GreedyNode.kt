package io.github.proxyfox.string.parser

class GreedyNode(name: String) : Node {
    override fun parse(string: String, index: Int): Int {
        return index
    }

    override fun getSubNodes(): List<Node> = ArrayList()

    override fun addSubNode(node: Node) = Unit

    override fun execute(holder: MessageHolder) {
        TODO("Not yet implemented")
    }
}