/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.database.PkId
import dev.proxyfox.database.generateToken
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.Record
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author Ampflower
 **/
@Serializable
open class SystemRecord : Record {
    var id: PkId = ""
    var users: ArrayList<ULong> = ArrayList()
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null
    var timezone: String? = null
    var timestamp: Instant = Clock.System.now()
    var token: String = generateToken()

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: PkId? = null
    var autoType: AutoProxyMode = AutoProxyMode.OFF
    var trust: HashMap<ULong, TrustLevel> = HashMap()

    val showName get() = name?.let { "$it [`$id`]" } ?: "`$id`"

    override fun toMongo() = MongoSystemRecord(this)
}

@Serializable
class MongoSystemRecord : SystemRecord, MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()

    constructor(record: SystemRecord) {
        id = record.id
        users = record.users
        name = record.name
        description = record.description
        tag = record.tag
        pronouns = record.pronouns
        color = record.color
        avatarUrl = record.avatarUrl
        timezone = record.timezone
        timestamp = record.timestamp
        token = record.token
        autoProxy = record.autoProxy
        autoType = record.autoType
        trust = record.trust
    }

    override fun toMongo() = this
}
