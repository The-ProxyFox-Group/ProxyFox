/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.proxyfox.bot.kord
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max

/**
 * Utilities for using webhooks
 * @author Oliver
 * */
object WebhookUtil {
    suspend fun prepareMessage(
        message: GuildMessage,
        content: String,
        system: SystemRecord,
        member: MemberRecord,
        proxy: MemberProxyTagRecord?,
        serverMember: MemberServerSettingsRecord?,
        moderationDelay: Long = 500L,
    ): ProxyContext? {
        var messageContent = content
        if (!member.keepProxy && proxy != null)
            messageContent = proxy.trim(messageContent).trim()
        if (messageContent.isBlank() && message.attachments.isEmpty()) return null

        return ProxyContext(
            messageContent,
            createOrFetchWebhookFromCache(message.channel.asChannel()),
            message,
            system,
            member,
            proxy,
            if (message.channel is ThreadChannelBehavior) message.channel.id else null,

            resolvedUsername = serverMember?.nickname ?: member.displayName ?: member.name,
            resolvedAvatar = serverMember?.avatarUrl ?: member.avatarUrl ?: system.avatarUrl,
            moderationDelay = max(moderationDelay, 0L),
        )
    }

    suspend fun createOrFetchWebhookFromCache(channel: Channel): WebhookHolder {
        // Try to fetch webhook from cache
        var id = when(channel) {
            is ThreadChannel -> channel.parentId.value
            else -> channel.id.value
        }
        WebhookCache[id]?.let {
            return@createOrFetchWebhookFromCache it
        }
        return createOrFetchWebhook(channel)
    }

    private suspend fun createOrFetchWebhook(channel: Channel): WebhookHolder {
        when (channel) {
            is ThreadChannel -> return createOrFetchWebhook(channel.getParent())
            is TextChannel -> {
                // Try to fetch webhook from channel
                channel.webhooks.firstOrNull { it.creatorId == kord.selfId }?.let {
                    val holder = it.toHolder()
                    WebhookCache[channel.id.value] = holder
                    return holder
                }
                // Create webhook
                channel.createWebhook("ProxyFox Webhook") {}.let {
                    val holder = it.toHolder()
                    WebhookCache[channel.id.value] = holder
                    return holder
                }
            }
            else -> error("Provided channel is not a thread or text channel")
        }
    }
}