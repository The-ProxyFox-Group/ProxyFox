package io.github.proxyfox.string.dsl

import io.github.proxyfox.string.node.GreedyNode
import io.github.proxyfox.string.node.LiteralNode
import io.github.proxyfox.string.node.Node
import io.github.proxyfox.string.node.StringNode
import io.github.proxyfox.string.parser.MessageHolder

fun literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: Node.() -> Unit
): LiteralNode = LiteralNode(name, executor).apply(action)

fun Node.literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: Node.() -> Unit
): LiteralNode {
    val node = LiteralNode(name, executor).apply(action)
    addSubNode(node)
    return node
}

fun Node.greedy(
    name: String,
    executor: suspend MessageHolder.() -> String
): GreedyNode {
    val node = GreedyNode(name, executor)
    addSubNode(node)
    return node
}

fun Node.string(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: Node.() -> Unit
): StringNode {
    val node = StringNode(name, executor).apply(action)
    addSubNode(node)
    return node
}