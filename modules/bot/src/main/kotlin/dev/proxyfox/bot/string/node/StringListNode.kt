package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class StringListNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override fun parse(string: String, index: Int, holder: MessageHolder): Int {
        if (string.length < index) return index
        val newString = string.substring(index)
        var i = index
        val arr = ArrayList<String>()
        while (i < string.length) {
            when (newString[i]) {
                '"' -> {
                    var out = ""
                    for (j in newString.indices) {
                        if (newString[j] == '"') {
                            arr.add(out)
                            i += j
                            continue
                        }
                        out += newString[j].toString()
                    }
                    arr.add(out)
                }
                '\'' -> {
                    var out = ""
                    for (j in newString.indices) {
                        if (newString[j] == '\'') {
                            arr.add(out)
                            i += j
                            continue
                        }
                        out += newString[j].toString()
                    }
                    arr.add(out)
                }
                else -> {
                    var out = ""
                    for (j in newString.indices) {
                        if (newString[j] == ' ') {
                            arr.add(out)
                            i += j
                            continue
                        }
                        out += newString[j].toString()
                    }
                    arr.add(out)
                }
            }
            i = string.length
        }
        holder.params[name] = arr.toTypedArray()
        return string.length
    }

    override fun getSubNodes(): Array<Node> = arrayOf()

    override fun addSubNode(node: Node) = Unit

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}