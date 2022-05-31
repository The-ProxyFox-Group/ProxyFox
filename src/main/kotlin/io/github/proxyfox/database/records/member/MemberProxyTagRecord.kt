package io.github.proxyfox.database.records.member

import org.bson.types.ObjectId

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author KJP12
 **/
class MemberProxyTagRecord {
    var _id: ObjectId = ObjectId()
    var systemId: String = ""
    var memberId: String = ""
    var prefix: String = ""
    var suffix: String = ""

    fun test(message: String): Boolean {
        var pre = true
        if (prefix != null) pre = message.startsWith(prefix!!)
        var suf = true
        if (suffix != null) suf = message.startsWith(suffix!!)
        return pre && suf
    }

    fun trim(message: String): String {
        return message.substring(prefix.length, message.length - suffix.length)
    }
}
