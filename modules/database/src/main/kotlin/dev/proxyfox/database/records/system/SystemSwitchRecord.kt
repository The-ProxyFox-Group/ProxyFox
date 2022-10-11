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
import dev.proxyfox.jackson.InstantDeserializer
import dev.proxyfox.jackson.InstantSerializer
import org.bson.types.ObjectId
import java.time.Instant

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author KJP12
 **/
class SystemSwitchRecord {
    var _id: ObjectId = ObjectId()
    var systemId: String
    var id: String
    var memberIds: List<String>

    @JsonDeserialize(using = InstantDeserializer::class)
    @JsonSerialize(using = InstantSerializer::class)
    var timestamp: Instant
        set(inst) {
            field = inst.minusNanos(inst.nano.mod(1000).toLong())
        }

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(systemId: String = "", id: String = "", memberIds: List<String> = ArrayList(), timestamp: Instant? = null) {
        this.systemId = systemId
        this.id = id
        this.memberIds = memberIds
        this.timestamp = timestamp ?: Instant.now()
    }
}