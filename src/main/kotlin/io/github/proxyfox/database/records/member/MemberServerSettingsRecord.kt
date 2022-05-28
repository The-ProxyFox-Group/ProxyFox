package io.github.proxyfox.database.records.member

import dev.kord.common.entity.Snowflake

// Created 2022-09-04T14:16:19

/**
 * A mutable record representing a member's server settings.
 *
 * @author KJP12
 **/
data class MemberServerSettingsRecord(
    val serverId: Snowflake,
    val systemId: String,
    val memberId: String,
    var avatarUrl: String?,
    var nickname: String?,
    /** Whether the member should be auto-proxied in the server. */
    var autoProxy: Boolean,
    var proxyEnabled: Boolean
)
