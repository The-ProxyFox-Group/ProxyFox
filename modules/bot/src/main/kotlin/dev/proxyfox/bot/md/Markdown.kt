package dev.proxyfox.bot.md

interface MarkdownNode {
    val length: Int

    override fun toString(): String
}

class MarkdownString(val string: String) : MarkdownNode {
    override val length: Int
        get() = string.length

    override fun toString(): String = string
}

class BaseMarkdown(val symbol: String) : MarkdownNode {
    val values = ArrayList<MarkdownNode>()

    override val length: Int
        get() {
            var int = symbol.length*2
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
}

// TODO: Parse out more complex markdowns
fun parseMarkdown(string: String, symbol: String = "", i: Int = 0): MarkdownNode {
    val base = BaseMarkdown(symbol)
    var idx = i
    var lastIdx = idx
    while (idx < string.length) {
        val substr = string.substring(idx)
        for (sym in MarkdownSymbols.values()) {
            if (substr.startsWith(sym.symbol)) {
                base.values.add(MarkdownString(string.substring(lastIdx, idx)))
                val new = parseMarkdown(string, sym.symbol, idx+symbol.length)
                idx += new.length
                lastIdx = idx
            }
        }
        idx++
    }
    base.values.add(MarkdownString(string.substring(lastIdx)))
    return base
}