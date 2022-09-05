package dev.proxyfox.bot.string.node

import dev.proxyfox.bot.string.parser.MessageHolder

class UnixNode(val name: String, val executor: suspend MessageHolder.() -> String) : Node {
    override val type: NodeType = NodeType.VARIABLE

    private val literalNodes: ArrayList<Node> = ArrayList()
    private val stringNodes: ArrayList<Node> = ArrayList()
    private val greedyNodes: ArrayList<Node> = ArrayList()

    override fun parse(string: String, holder: MessageHolder): Int {
        if (string.isEmpty()) return 0
        var idx = 0
        val arr = ArrayList<String>()
        while (idx < string.length) {
            while (idx < string.length) {
                if (string[idx] != ' ') break
                idx++
            }
            var substr = string.substring(idx)
            val start =
                if (substr.startsWith("--")) "--"
                else if (substr.startsWith("-")) "-"
                else break
            idx += start.length
            substr = string.substring(idx)
            var out = ""
            for (i in substr) {
                if (i == ' ')
                    break
                out += i
            }
            arr.add(out)
            idx += out.length
        }
        holder.params[name] = arr.toTypedArray()
        return idx
    }

    override fun getSubNodes(): Array<Node> {
        val literalArray: Array<Node> = literalNodes.toTypedArray()
        val stringArray: Array<Node> = stringNodes.toTypedArray()
        val greedyArray: Array<Node> = greedyNodes.toTypedArray()
        return literalArray + stringArray + greedyArray
    }

    override fun addSubNode(node: Node) {
        when (node.type) {
            NodeType.LITERAL -> literalNodes.add(node)
            NodeType.VARIABLE -> stringNodes.add(node)
            NodeType.GREEDY -> greedyNodes.add(node)
        }
    }

    override suspend fun execute(holder: MessageHolder): String = holder.executor()
}
