/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMicrosecondSerializer
import dev.proxyfox.database.records.MongoRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author Ampflower
 **/
@Serializable
class SystemSwitchRecord : MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()
    var systemId: PkId
    var id: PkId
    var memberIds: List<PkId>

    @Serializable(InstantLongMicrosecondSerializer::class)
    var timestamp: Instant
        set(inst) {
            field = inst.minus(inst.nanosecondsOfSecond % 1000, DateTimeUnit.NANOSECOND)
        }

    constructor(systemId: PkId = "", id: PkId = "", memberIds: List<PkId> = ArrayList(), timestamp: Instant? = null) {
        this.systemId = systemId
        this.id = id
        this.memberIds = memberIds
        this.timestamp = timestamp ?: Clock.System.now()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is SystemSwitchRecord
                && other.systemId == systemId
                && other.memberIds == memberIds
                && other.timestamp == timestamp
    }

    override fun hashCode(): Int {
        var result = systemId.hashCode()
        result = 31 * result + memberIds.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }

    override fun toString(): String {
        return "Switch{systemId=$systemId, memberIds=$memberIds, timestamp=$timestamp}"
    }
}