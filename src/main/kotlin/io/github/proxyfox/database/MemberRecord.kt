package io.github.proxyfox.database

import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author KJP12
 **/
data class MemberRecord(
    val id: String,
    val systemId: String,
    var name: String,
    var displayName: String? = null,
    var description: String? = null,
    var pronouns: String? = null,
    var color: Int = -1,
    var avatarUrl: String? = null,
    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean = false,
    var messageCount: Long = 0L,
    var created: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
)
