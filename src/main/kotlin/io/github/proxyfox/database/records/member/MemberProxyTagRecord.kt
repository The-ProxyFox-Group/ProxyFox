package io.github.proxyfox.database.records.member

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author Ampflower
 **/
class MemberProxyTagRecord {
    var systemId: String = ""
    var memberId: String = ""
    var prefix: String? = null
    var suffix: String? = null

    fun test(message: String): Boolean {
        var pre = true
        if (prefix != null) pre = message.startsWith(prefix!!)
        var suf = true
        if (suffix != null) suf = message.startsWith(suffix!!)
        return pre && suf
    }
}
