/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Webhook
import dev.kord.rest.builder.message.create.WebhookMessageCreateBuilder
import dev.kord.rest.builder.message.modify.WebhookMessageModifyBuilder
import dev.proxyfox.bot.kord

data class WebhookHolder(
    val id: Snowflake,
    val token: String
) {
    suspend inline fun execute(threadId: Snowflake?, builder: WebhookMessageCreateBuilder.() -> Unit): DiscordMessage {
        // We assert that the return is always non-null, for as we wait.
        return kord.rest.webhook.executeWebhook(id, token, true, threadId, builder)!!
    }

    suspend inline fun edit(messageId: Snowflake, threadId: Snowflake?, builder: WebhookMessageModifyBuilder.() -> Unit): DiscordMessage {
        return kord.rest.webhook.editWebhookMessage(id, token, messageId, threadId, builder)
    }

    suspend inline fun delete(messageId: Snowflake, threadId: Snowflake?) {
        kord.rest.webhook.deleteWebhookMessage(id, token, messageId, threadId)
    }
}

fun Webhook.toHolder(): WebhookHolder = WebhookHolder(id, token!!)