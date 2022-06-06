package dev.proxyfox.bot.string.dsl

import dev.proxyfox.common.applyAsync
import dev.proxyfox.bot.string.node.GreedyNode
import dev.proxyfox.bot.string.node.LiteralNode
import dev.proxyfox.bot.string.node.Node
import dev.proxyfox.bot.string.node.StringNode
import dev.proxyfox.bot.string.parser.MessageHolder

suspend fun literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode = LiteralNode(name, executor).applyAsync(action)

fun literal(
    name: String,
    executor: suspend MessageHolder.() -> String
): LiteralNode = LiteralNode(name, executor)

suspend fun Node.literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode {
    val node = LiteralNode(name, executor).applyAsync(action)
    addSubNode(node)
    return node
}

fun Node.literal(
    name: String,
    executor: suspend suspend MessageHolder.() -> String
): LiteralNode {
    val node = LiteralNode(name, executor)
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

suspend fun Node.string(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): StringNode {
    val node = StringNode(name, executor).applyAsync(action)
    addSubNode(node)
    return node
}

fun Node.string(
    name: String,
    executor: suspend MessageHolder.() -> String
): StringNode {
    val node = StringNode(name, executor)
    addSubNode(node)
    return node
}