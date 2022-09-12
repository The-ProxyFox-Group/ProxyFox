/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.md

interface MarkdownNode {
    val length: Int
    val trueLength: Int

    override fun toString(): String
    fun substring(len: Int): MarkdownNode
}

class MarkdownString(val string: String) : MarkdownNode {
    override val length: Int
        get() = string.length
    override val trueLength: Int
        get() = string.length

    override fun toString(): String = string
    override fun substring(len: Int): MarkdownNode {
        return MarkdownString(string.substring(0, len.coerceAtMost(string.length)))
    }
}

class BaseMarkdown(val symbol: String) : MarkdownNode {
    val values = ArrayList<MarkdownNode>()

    override val length: Int
        get() {
            var int = 0
            for (value in values) {
                int += value.length
            }
            return int
        }
    override val trueLength: Int
        get() {
            var int = symbol.length + symbol.length
            for (value in values) {
                int += value.length
            }
            return int
        }

    override fun toString(): String {
        var out = ""
        out += symbol
        for (value in values) {
            out += value.toString()
        }
        out += symbol
        return out
    }

    override fun substring(len: Int): MarkdownNode {
        if (trueLength < len) return this
        var i = 0
        val out = BaseMarkdown(symbol)
        for (value in values) {
            if (i + value.length > len) {
                out.values.add(value.substring(len - i))
                break
            }
            out.values.add(value)
            i += value.length
        }
        return out
    }
}

enum class MarkdownSymbols(val symbol: String) {
    CODE_MULTILINE("```"),
    CODE_DOUBLE("``"),
    SPOILER("||"),
    BOLD("**"),
    STRIKETHROUGH("~~"),
    UNDERLINE("__"),
    ITALIC_STAR("*"),
    ITALIC_UNDER("_"),
    CODE("`")
}

// TODO: Parse out more complex markdowns
fun parseMarkdown(string: String, symbol: String = ""): BaseMarkdown {
    val base = BaseMarkdown(symbol)
    var idx = 0
    var lastIdx = 0
    while (idx < string.length) {
        val substr = string.substring(idx)
        for (sym in MarkdownSymbols.values()) {
            if (substr.startsWith(sym.symbol)) {
                var currIdx = sym.symbol.length
                while (currIdx < substr.length) {
                    val subsubstr = substr.substring(currIdx)
                    if (subsubstr.startsWith(sym.symbol)) {
                        base.values.add(MarkdownString(string.substring(lastIdx, idx)))
                        val md = parseMarkdown(
                            substr.substring(
                                sym.symbol.length,
                                (currIdx).coerceAtMost(substr.length)
                            ),
                            sym.symbol
                        )
                        base.values.add(md)
                        idx += md.trueLength
                        lastIdx = idx
                        break
                    }
                    currIdx++
                }
                break
            }
        }
        idx++
    }
    base.values.add(MarkdownString(string.substring(lastIdx, idx.coerceAtMost(string.length))))
    return base
}