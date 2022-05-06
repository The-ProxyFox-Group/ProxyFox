package io.github.proxyfox.database.records.member

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author KJP12
 **/
data class MemberProxyTagRecord(
    val systemId: String,
    val memberId: String,
    var prefix: String?,
    var suffix: String?
)
