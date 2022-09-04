package dev.proxyfox.bot.md

interface MarkdownNode

class MarkdownString(val string: String) : MarkdownNode

class BaseMarkdown(val symbol: String) : MarkdownNode {
    val values = ArrayList<MarkdownNode>()
}

enum class MarkdownSymbols(val symbol: String) {
    CODE_MULTILINE("```"),
    SPOILER("||"),
    BOLD("**"),
    STRIKETHROUGH("~~"),
    UNDERLINE("__"),
    ITALIC_STAR("*"),
    ITALIC_UNDER("_"),
    CODE("`")
    ;
    fun create() = BaseMarkdown(symbol)
}

fun parseMarkdown(string: String, symbol: String = "", i: Int = 0): MarkdownNode {
    val base = BaseMarkdown(symbol)
    var idx = i
    var lastIdx = idx
    while (idx < string.length) {
        val substr = string.substring(idx)
        for (sym in MarkdownSymbols.values()) {
            if (substr.startsWith(sym.symbol)) {
                base.values.add(MarkdownString(string.substring(lastIdx, idx)))
                idx += sym.symbol.length
                TODO("Parse out symbol")

                lastIdx = idx
            }
        }
        idx++
    }

    return base
}