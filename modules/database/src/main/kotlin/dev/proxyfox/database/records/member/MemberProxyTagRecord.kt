/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import org.bson.types.ObjectId

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author Ampflower
 **/
class MemberProxyTagRecord {
    var _id: ObjectId = ObjectId()
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

    fun trim(message: String): String {
        val pLength = prefix?.length ?: 0
        val slength = suffix?.length ?: 0
        return message.substring(pLength, message.length - slength)
    }

    override fun toString() = "${prefix}text$suffix"
}
