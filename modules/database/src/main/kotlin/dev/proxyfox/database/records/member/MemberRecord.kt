/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import dev.proxyfox.database.*
import dev.proxyfox.database.records.MongoRecord
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author Ampflower
 **/
class MemberRecord() : MongoRecord {
    constructor(id: PkId, systemId: PkId, name: String) : this() {
        this.id = id
        this.systemId = systemId
        this.name = name
    }

    override var _id: ObjectId = ObjectId()

    var id: PkId = ""
    var systemId: PkId = ""
    var name: String = ""
    var displayName: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null

    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean = false
    var autoProxy: Boolean = true
    var messageCount: ULong = 0UL
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var birthday: LocalDate? = null
    var age: String? = null
    var role: String? = null

    fun asString() = displayName?.let { "$it ($name)" } ?: name

    fun showDisplayName() = displayName ?: name

    suspend fun serverName(serverId: ULong) =
        if (serverId == 0UL) null else database.fetchMemberServerSettingsFromSystemAndMember(serverId, systemId, id)?.nickname
}
