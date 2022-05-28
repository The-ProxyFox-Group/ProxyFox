package io.github.proxyfox.webhook

import com.google.common.cache.CacheBuilder
import dev.kord.common.entity.Snowflake
import java.util.concurrent.TimeUnit

/**
 * A cache for webhooks
 * @author Oliver
 * */
object WebhookCache {
    val webhookCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build<Snowflake, WebhookHolder>()

    operator fun get(key: Snowflake): WebhookHolder? = webhookCache.getIfPresent(key)
    operator fun set(key: Snowflake, value: WebhookHolder) {
        webhookCache.put(key, value)
    }
}