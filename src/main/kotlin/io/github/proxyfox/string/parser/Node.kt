package io.github.proxyfox.string.parser

interface Node {
    fun parse(string: String, index: Int): Int
    fun getSubNodes(): List<Node>
    fun addSubNode(node: Node)
}