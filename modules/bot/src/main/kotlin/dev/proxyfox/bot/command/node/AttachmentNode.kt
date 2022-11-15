package dev.proxyfox.bot.command.node

import dev.kord.core.entity.Attachment
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.command.NodeActionParam
import dev.proxyfox.command.NodeHolder
import dev.proxyfox.command.node.CommandNode
import dev.proxyfox.command.node.Priority

class AttachmentNode<T, C: DiscordContext<T>>(override val name: String) : CommandNode<T, C>() {
    override val priority: Priority = Priority.SEMI_STATIC
    override fun parse(str: String, ctx: C): Int {
        val attachment = ctx.getAttachment() ?: return -1
        ctx[name] = attachment
        return 0
    }
}

suspend fun <T, C: DiscordContext<T>> NodeHolder<T,C>.attachment(
    name: String,
    action: NodeActionParam<T, C, Attachment>
): CommandNode<T, C> {
    val node = AttachmentNode<T, C>(name)
    node.action {
        this[name] ?: throw NullPointerException("Parameter $name for $command is null!")
    }
    addNode(node)
    return node
}