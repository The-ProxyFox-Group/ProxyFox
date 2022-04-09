package io.github.proxyfox.database

import java.time.OffsetDateTime

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author KJP12
 **/
data class MemberRecord(
    val id: String,
    val systemId: String,
    var name: String?,
    var displayName: String?,
    var description: String?,
    var pronouns: String?,
    var color: String?,
    var avatarUrl: String?,
    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean,
    var messageCount: Long,
    var created: OffsetDateTime
)
