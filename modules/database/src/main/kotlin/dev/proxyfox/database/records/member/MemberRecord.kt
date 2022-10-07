/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import dev.proxyfox.database.database
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author KJP12
 **/
class MemberRecord {
    var _id: ObjectId = ObjectId()

    var id: String = ""
    var systemId: String = ""
    var name: String = ""
    var displayName: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null

    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean = false
    var messageCount: ULong = 0UL
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var birthday: LocalDate? = null
    var age: String? = null
    var role: String? = null

    fun asString() = displayName?.let { "$it ($name)" } ?: name

    suspend fun serverName(serverId: ULong) =
        if (serverId == 0UL) null else database.fetchMemberServerSettingsFromSystemAndMember(serverId, systemId, id)?.nickname
}
