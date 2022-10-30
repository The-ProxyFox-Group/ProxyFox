/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dev.proxyfox.database.*
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.etc.jackson.InstantDeserializer
import dev.proxyfox.database.etc.jackson.InstantSerializer
import org.bson.types.ObjectId
import java.time.Instant

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author KJP12
 **/
class SystemSwitchRecord : MongoRecord {
    override var _id: ObjectId = ObjectId()
    var systemId: PkId
    var id: PkId
    var memberIds: List<PkId>

    @JsonDeserialize(using = InstantDeserializer::class)
    @JsonSerialize(using = InstantSerializer::class)
    var timestamp: Instant
        set(inst) {
            field = inst.minusNanos(inst.nano.mod(1000).toLong())
        }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(systemId: PkId = "", id: PkId = "", memberIds: List<PkId> = ArrayList(), timestamp: Instant? = null) {
        this.systemId = systemId
        this.id = id
        this.memberIds = memberIds
        this.timestamp = timestamp ?: Instant.now()
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