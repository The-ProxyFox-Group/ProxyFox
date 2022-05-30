package io.github.proxyfox

import dev.kord.core.event.message.MessageCreateEvent
import io.github.proxyfox.string.parser.parseString
import io.github.proxyfox.webhook.WebhookUtil

suspend fun MessageCreateEvent.onMessageCreate() {
    // Return if bot
    if (message.webhookId != null || message.author!!.isBot) return

    // Get message content to check with regex
    val content = message.content
    if (prefixRegex.matches(content)) {
        // Remove the prefix to pass into dispatcher
        val contentWithoutRegex = content.substring(3)
        // Run the command
        val output = parseString(contentWithoutRegex, message)
        // Send output message if exists
        if (output!!.isNotBlank())
            message.channel.createMessage(output)
    } else {
        // Proxy the message
        val proxy = database.getProxyTagFromMessage(message.author!!.id.value.toString(), content)
        if (proxy != null) {
            val member = database.getMemberById(proxy.systemId, proxy.memberId)!!
            WebhookUtil.prepareMessage(message, member, proxy).send()
        }
    }
}