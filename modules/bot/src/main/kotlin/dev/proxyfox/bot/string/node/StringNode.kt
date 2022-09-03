package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class StringNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    private val literalNodes: ArrayList<LiteralNode> = ArrayList()
    private val stringNodes: ArrayList<StringNode> = ArrayList()
    private val greedyNodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, index: Int, holder: MessageHolder): Int {
        if (string.length < index) return index
        val newString = string.substring(index)
        when (newString[0]) {
            '"' -> {
                var out = ""
                for (i in newString.substring(1).indices) {
                    if (newString[i+1] == '"') {
                        holder.params["owo"] = arrayOf(out)
                        return index + i + 2
                    }
                    out += newString[i+1].toString()
                }
                holder.params["owo"] = arrayOf(out)
            }
            '\'' -> {
                var out = ""
                for (i in newString.substring(1).indices) {
                    if (newString[i+1] == '\'') {
                        holder.params["owo"] = arrayOf(out)
                        return index + i + 2
                    }
                    out += newString[i+1].toString()
                }
                holder.params["owo"] = arrayOf(out)
            }
            else -> {
                var out = ""
                for (i in newString.indices) {
                    if (newString[i] == ' ') {
                        holder.params["owo"] = arrayOf(out)
                        return index + i
                    }
                    out += newString[i].toString()
                }
                holder.params["owo"] = arrayOf(out)
            }
        }
        return string.length
    }

    override fun getSubNodes(): Array<Node> {
        val literalArray: Array<Node> = literalNodes.toTypedArray()
        val stringArray: Array<Node> = stringNodes.toTypedArray()
        val greedyArray: Array<Node> = greedyNodes.toTypedArray()
        return literalArray + stringArray + greedyArray
    }

    override fun addSubNode(node: Node) {
        when (node) {
            is LiteralNode -> literalNodes.add(node)
            is StringNode -> stringNodes.add(node)
            is GreedyNode -> greedyNodes.add(node)
            is StringListNode -> greedyNodes.add(node)
        }
    }

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}