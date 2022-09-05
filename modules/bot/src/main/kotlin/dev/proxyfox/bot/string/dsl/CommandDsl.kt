package dev.proxyfox.bot.string.dsl

import dev.proxyfox.bot.string.node.*
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.common.applyAsync

suspend fun literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode = LiteralNode(arrayOf(name), executor).applyAsync(action)

fun literal(
    name: String,
    executor: suspend MessageHolder.() -> String
): LiteralNode = LiteralNode(arrayOf(name), executor)

suspend fun Node.literal(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode {
    val node = LiteralNode(arrayOf(name), executor).applyAsync(action)
    addSubNode(node)
    return node
}

fun Node.literal(
    name: String,
    executor: suspend suspend MessageHolder.() -> String
): LiteralNode {
    val node = LiteralNode(arrayOf(name), executor)
    addSubNode(node)
    return node
}

suspend fun literal(
    names: Array<out String>,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode = LiteralNode(names, executor).applyAsync(action)

fun literal(
    names: Array<out String>,
    executor: suspend MessageHolder.() -> String
): LiteralNode = LiteralNode(names, executor)

suspend fun Node.literal(
    names: Array<out String>,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): LiteralNode {
    val node = LiteralNode(names, executor).applyAsync(action)
    addSubNode(node)
    return node
}

fun Node.literal(
    names: Array<out String>,
    executor: suspend suspend MessageHolder.() -> String
): LiteralNode {
    val node = LiteralNode(names, executor)
    addSubNode(node)
    return node
}

suspend fun Node.unix(
    name: String,
    executor: suspend MessageHolder.() -> String,
    action: suspend Node.() -> Unit
): UnixNode {
    val node = UnixNode(name, executor).applyAsync(action)
    addSubNode(node)
    return node
}

fun Node.unix(
    name: Array<String>,
    executor: suspend MessageHolder.() -> String
): LiteralNode {
    val node = LiteralNode(name.flatMap { listOf("-$it", "--$it") }.toTypedArray(), executor)
    addSubNode(node)
    return node
}

fun Node.unix(
    name: String,
    executor: suspend MessageHolder.() -> String
): LiteralNode {
    val node = LiteralNode(arrayOf("-$name", "--$name"), executor)
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

fun Node.stringList(
    name: String,
    executor: suspend MessageHolder.() -> String
): StringListNode {
    val node = StringListNode(name, executor)
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