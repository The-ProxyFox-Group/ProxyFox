package io.github.proxyfox.database.records.system

import io.github.proxyfox.database.records.misc.AutoProxyMode
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Duration

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author KJP12
 **/
data class SystemRecord(
    val id: String,
    var name: String? = null,
    var description: String? = null,
    var tag: String? = null,
    var avatarUrl: String? = null,
    var timezone: String? = null,
    var created: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null,
    var autoProxyMode: AutoProxyMode = AutoProxyMode.OFF,
    var autoProxyTimeout: Duration? = null
)
