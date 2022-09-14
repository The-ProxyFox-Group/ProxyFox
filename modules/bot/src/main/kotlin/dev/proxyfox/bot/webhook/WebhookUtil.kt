/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.proxyfox.bot.kord
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlinx.coroutines.flow.firstOrNull

/**
 * Utilities for using webhooks
 * @author Oliver
 * */
object WebhookUtil {
    suspend fun prepareMessage(
            message: Message,
            system: SystemRecord,
            member: MemberRecord,
            proxy: MemberProxyTagRecord?
    ) = ProxyContext(
        message.content,
        createOrFetchWebhookFromCache(message.channel.asChannel() as TextChannel),
        message,
        system,
        member,
        proxy
    )

    private suspend fun createOrFetchWebhookFromCache(channel: TextChannel): WebhookHolder {
        // Try to fetch webhook from cache
        WebhookCache[channel.id]?.let {
            return@createOrFetchWebhookFromCache it
        }
        return createOrFetchWebhook(channel)
    }

    private suspend fun createOrFetchWebhook(channel: TextChannel): WebhookHolder {
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