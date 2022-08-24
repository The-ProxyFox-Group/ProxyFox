package dev.proxyfox.common

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Important functions and variables needed for proxyfox
 * @author Oliver
 * */

val logger = LoggerFactory.getLogger("ProxyFox")
val prefixRegex = Regex("^pf[>;!].*", RegexOption.IGNORE_CASE)

fun printFancy(input: String) {
    val edges = "*".repeat(input.length + 4)
    logger.info(edges)
    logger.info("* $input *")
    logger.info(edges)
}

fun printStep(input: String, step: Int) {
    val add = "  ".repeat(step)
    logger.info(step.toString() + add + input)
}

fun String.toColor(): Int {
    return try {
        if (startsWith("#"))
            Integer.valueOf(substring(1), 16)
        else if (startsWith("0x"))
            Integer.decode(this)
        else
            toInt(16)
    } catch (err: Throwable) {
        0
    }
}

fun Int.fromColor(): String {
    var string = toString(16)
    if (string == "-1") string = "0"
    return "#${string.padStart(7 - string.length, '0')}"
}

@OptIn(DelicateCoroutinesApi::class)
fun runAsync(action: suspend () -> Unit): Int {
    GlobalScope.launch {
        action()
    }
    return 0
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <T> T.applyAsync(block: suspend T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}