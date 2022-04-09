package io.github.proxyfox.database

import java.time.OffsetDateTime
import java.time.Instant

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author KJP12
 **/
data class SystemRecord(
    val id: String,
    var name: String?,
    var description: String?,
    var tag: String?,
    var avatarUrl: String?,
    var timezone: String?,
    var created: String?,
    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String?
)
