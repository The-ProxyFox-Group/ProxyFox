/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import dev.proxyfox.database.PkId
import dev.proxyfox.database.database
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.etc.ktx.serializaton.LocalDateLongMillisecondSerializer
import dev.proxyfox.database.records.MongoRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author Ampflower
 **/
@Serializable
class MemberRecord() : MongoRecord {
    constructor(id: PkId, systemId: PkId, name: String) : this() {
        this.id = id
        this.systemId = systemId
        this.name = name
    }

    @Contextual
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

    @Serializable(InstantLongMillisecondSerializer::class)
    var timestamp: Instant = Clock.System.now()

    @Serializable(LocalDateLongMillisecondSerializer::class)
    var birthday: LocalDate? = null
    var age: String? = null
    var role: String? = null

    fun asString() = displayName?.let { "$it ($name)" } ?: name

    fun showDisplayName() = displayName ?: name

    suspend fun serverName(serverId: ULong) =
        if (serverId == 0UL) null else database.fetchMemberServerSettingsFromSystemAndMember(serverId, systemId, id)?.nickname
}
