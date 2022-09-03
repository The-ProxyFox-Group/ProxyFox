package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class StringNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    private val literalNodes: ArrayList<LiteralNode> = ArrayList()
    private val stringNodes: ArrayList<StringNode> = ArrayList()
    private val greedyNodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        when (string[0]) {
            '"' -> {
                var out = ""
                for (i in string.substring(1).indices) {
                    if (string[i+1] == '"') {
                        holder.params[name] = arrayOf(out)
                        return i + 2
                    }
                    out += string[i+1].toString()
                }
                holder.params[name] = arrayOf(out)
            }
            '\'' -> {
                var out = ""
                for (i in string.substring(1).indices) {
                    if (string[i+1] == '\'') {
                        holder.params[name] = arrayOf(out)
                        return i + 2
                    }
                    out += string[i+1].toString()
                }
                holder.params[name] = arrayOf(out)
            }
            else -> {
                var out = ""
                for (i in string.indices) {
                    if (string[i] == ' ') {
                        holder.params[name] = arrayOf(out)
                        return i
                    }
                    out += string[i].toString()
                }
                holder.params[name] = arrayOf(out)
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