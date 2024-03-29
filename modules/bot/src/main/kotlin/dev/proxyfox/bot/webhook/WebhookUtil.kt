/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.cache.data.WebhookData
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.proxyfox.bot.getAs
import dev.proxyfox.bot.httpUri
import dev.proxyfox.bot.httpUriOrNull
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
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
            createOrFetchWebhookFromCache(message.channel.asChannelOf()),
            message,
            system,
            member,
            proxy,
            message.channel.getAs<ThreadChannel>()?.id,

            resolvedUsername = serverMember?.nickname ?: member.displayName ?: member.name,
            resolvedAvatar = serverMember?.avatarUrl.httpUriOrNull() ?: member.avatarUrl.httpUriOrNull() ?: system.avatarUrl?.httpUri(),
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
        val kord = channel.kord
        val inst = if (channel is ThreadChannel) {
            channel.kord.getChannelOf(channel.parentId)!!
        } else {
            channel
        }

        kord.rest.webhook.getChannelWebhooks(inst.id).firstOrNull { it.applicationId == kord.selfId }?.let {
            val holder = Webhook(WebhookData.from(it), kord).toHolder()
            WebhookCache[channel.id.value] = holder
            return holder
        }

        // Create webhook
        kord.rest.webhook.createWebhook(inst.id, "ProxyFox Webhook") {}.let {
            val holder = Webhook(WebhookData.from(it), kord).toHolder()
            WebhookCache[channel.id.value] = holder
            return holder
        }
    }
}