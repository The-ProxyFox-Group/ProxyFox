package io.github.proxyfox.database.records.member

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author KJP12
 **/
class MemberProxyTagRecord {
    var systemId: String = ""
    var memberId: String = ""
    var prefix: String? = null
    var suffix: String? = null
}
