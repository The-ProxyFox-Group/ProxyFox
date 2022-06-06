package dev.proxyfox.bot.webhook

import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.proxyfox.bot.kord
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import kotlinx.coroutines.flow.firstOrNull

/**
 * Utilities for using webhooks
 * @author Oliver
 * */
object WebhookUtil {
    suspend fun prepareMessage(message: Message, member: MemberRecord, proxy: MemberProxyTagRecord): ProxyContext = ProxyContext(
        message.content,
        ArrayList(),
        fetchWebhook(message.channel.asChannel() as TextChannel),
        message,
        member,
        proxy
    )

    suspend fun fetchWebhook(channel: TextChannel): WebhookHolder {
        // Try to fetch webhook from cache
        WebhookCache[channel.id]?.let {
            return it
        }
        return createOrFetchWebhook(channel)
    }

    suspend fun createOrFetchWebhook(channel: TextChannel): WebhookHolder {
        // Try to fetch webhook from channel
        channel.webhooks.firstOrNull { it.creatorId == kord.selfId }?.let {
            val holder = it.toHolder()
            WebhookCache[channel.id] = holder
            return holder
        }
        // Create webhook
        channel.createWebhook("ProxyFox Webhook") {}.let {
            val holder = it.toHolder()
            WebhookCache[channel.id] = holder
            return holder
        }
    }
}