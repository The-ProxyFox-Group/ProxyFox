package io.github.proxyfox.database.records.member

import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author KJP12
 **/
class MemberRecord {
    var id: String = ""
    var systemId: String = ""
    var name: String = ""
    var displayName: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: Int = 0
    var avatarUrl: String? = null

    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean = false
    var messageCount: Long = 0L
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var birthday: String? = null
    var age: String? = null
    var role: String? = null
}
