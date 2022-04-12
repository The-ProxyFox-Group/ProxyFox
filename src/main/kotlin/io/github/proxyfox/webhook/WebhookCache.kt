package io.github.proxyfox.webhook

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Webhook

/**
 * A cache for webhooks
 * @author Oliver
 * */
object WebhookCache {
    val webhookCache = HashMap<Snowflake,Webhook>()

    operator fun get(key: Snowflake): Webhook? = webhookCache[key]
    operator fun set(key: Snowflake, value: Webhook) { webhookCache[key] = value }
}