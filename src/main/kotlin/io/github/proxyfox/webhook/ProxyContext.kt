package io.github.proxyfox.webhook

import dev.kord.common.entity.DiscordAttachment
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import io.github.proxyfox.kord

/**
 * Context for proxying
 * @author Oliver
 * */
data class ProxyContext(
    var messageContent: String,
    var attachments: List<DiscordAttachment>,
    var webhook: Webhook,
    var message: Message
) {
    suspend fun send() {
        kord.rest.webhook.executeWebhook(webhook.id,webhook.token!!,false) {
            content = messageContent
            return@executeWebhook
        }
    }
}