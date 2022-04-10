package io.github.proxyfox.webhook

import dev.kord.common.entity.DiscordAttachment
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.TextChannel
import io.github.proxyfox.kord
import kotlinx.coroutines.flow.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object WebhookUtil {
    suspend fun prepareMessage(message: Message): ProxyContext = ProxyContext(
        message.content,
        ArrayList(),
        fetchWebhook(message.channel.asChannel() as TextChannel),
        message
    )
    suspend fun fetchWebhook(channel: TextChannel): Webhook {
        // Try to fetch webhook from cache
        WebhookCache[channel.id]?.let {
            return it
        }
        // Try to fetch webhook from channel
        channel.webhooks.firstOrNull { it.creatorId == kord.selfId }?.let {
            WebhookCache[channel.id] = it
            return it
        }
        // Create webhook
        channel.createWebhook("ProxyFox Webhook") {}.let {
            WebhookCache[channel.id] = it
            return it
        }
    }
}