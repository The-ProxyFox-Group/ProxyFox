/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.common

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.lang.management.*
import java.nio.charset.Charset
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Important functions and variables needed for proxyfox
 * @author Oliver
 * */

val logger = LoggerFactory.getLogger("ProxyFox")

const val spacedDot = " \u2009• \u2009"
const val ellipsis = "…"

fun printFancy(input: String) {
    val edges = "*".repeat(input.length + 4)
    logger..edges.."* $input *"..edges
}

fun printStep(input: String, step: Int) {
    logger.."  " * step + input
}

operator fun Logger.rangeTo(string: String): Logger {
    info(string)
    return this
}

operator fun String.times(n: Int) = repeat(n)

fun String?.toColor(): Int {
    return if (this == null || this == "") -1 else (toUIntOrNull(16)?.toInt() ?: Integer.decode(this)) and 0xFFFFFF
}

fun Int.fromColor() = fromColorForExport()?.let { "#$it" }

fun Int.fromColorForExport() = if (this < 0) null else toString(16).run { padStart(6, '0') }

fun ceilDiv(x: Int, y: Int): Int {
    val r = x / y
    if ((x xor y >= 0) && r * x != y) return r + 1
    return r
}

inline fun String?.notBlank(action: (String) -> Unit) {
    if (!isNullOrBlank()) action(this)
}

fun String?.ifBlankThenNull(): String? = if (isNullOrBlank()) null else this

@OptIn(ExperimentalContracts::class)
suspend inline fun <T> T.applyAsync(block: suspend T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}

fun getGit(): Properties {
    val input = logger.javaClass.getResource("/git.properties")?.readText(Charset.defaultCharset())!!
    val properties = Properties()
    properties.load(StringReader(input))
    return properties
}

val gitProperties = getGit()

val hash = gitProperties["hash"] as String

val branch = gitProperties["branch"] as String

val version = gitProperties["version"] as String

val useragent =
    "ProxyFox/$version@$branch#$hash (+https://github.com/The-ProxyFox-Group/ProxyFox/; +https://proxyfox.dev/)"

class DebugException : Exception("Debug Exception - Do Not Report")

val threadMXBean = ManagementFactory.getThreadMXBean()

fun getMaxRam(): Long = Runtime.getRuntime().totalMemory()

fun getFreeRam(): Long = Runtime.getRuntime().freeMemory()

fun getRamUsage(): Long = getMaxRam() - getFreeRam()

fun getRamUsagePercentage(): Double = (getRamUsage().toDouble() / getMaxRam().toDouble()) * 100

fun getThreadCount() = threadMXBean.threadCount

fun Array<String>.trimEach() {
    forEachIndexed { i, s ->
        this[i] = s.trim()
    }
}

fun Throwable?.throwIfPresent() {
    throw this ?: return
}

inline fun <reified E : Event> Kord.onlyIf(
    crossinline getter: E.() -> Any?,
    compare: Any?,
    crossinline executor: suspend E.() -> Unit
) = on<E> {
    if (getter() == compare) executor()
}
