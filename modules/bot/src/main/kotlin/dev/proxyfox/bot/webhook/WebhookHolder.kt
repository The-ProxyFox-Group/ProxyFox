/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import dev.kord.core.entity.Webhook

data class WebhookHolder(
    val id: ULong,
    val token: String?
)

fun Webhook.toHolder(): WebhookHolder = WebhookHolder(id.value, token)