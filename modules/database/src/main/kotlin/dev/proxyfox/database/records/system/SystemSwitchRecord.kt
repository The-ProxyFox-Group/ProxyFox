/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author KJP12
 **/
class SystemSwitchRecord {
    var _id: ObjectId = ObjectId()
    var systemId: String = ""
    var id: String = ""
    var memberIds: List<String> = ArrayList()
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    constructor()

    constructor(systemId: String, id: String, memberIds: List<String>, timestamp: OffsetDateTime?) {
        this.systemId = systemId
        this.id = id
        this.memberIds = memberIds
        this.timestamp = timestamp ?: OffsetDateTime.now(ZoneOffset.UTC)
    }
}