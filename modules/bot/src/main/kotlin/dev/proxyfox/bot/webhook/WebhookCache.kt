/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.webhook

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

/**
 * A cache for webhooks
 * @author Oliver
 * */
object WebhookCache {
    private val webhookCache: Cache<ULong, WebhookHolder> = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build()

    operator fun get(key: ULong): WebhookHolder? = webhookCache.getIfPresent(key)
    operator fun set(key: ULong, value: WebhookHolder) {
        webhookCache.put(key, value)
    }
}