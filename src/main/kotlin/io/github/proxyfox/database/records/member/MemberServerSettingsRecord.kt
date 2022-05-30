package io.github.proxyfox.database.records.member

// Created 2022-09-04T14:16:19

/**
 * A mutable record representing a member's server settings.
 *
 * @author KJP12
 **/
class MemberServerSettingsRecord {
    var serverId: String = ""
    var systemId: String = ""
    var memberId: String = ""
    var avatarUrl: String? = null
    var nickname: String? = null

    /** Whether the member should be auto-proxied in the server. */
    var autoProxy: Boolean = false
    var proxyEnabled: Boolean = true
}