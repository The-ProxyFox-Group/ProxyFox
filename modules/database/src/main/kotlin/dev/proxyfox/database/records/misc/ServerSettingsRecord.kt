/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

import dev.proxyfox.database.records.MongoRecord
import org.bson.types.ObjectId

// Created 2022-10-04T21:06:30

/**
 * @author KJP12
 * @since ${version}
 **/
class ServerSettingsRecord : MongoRecord {
    override var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var proxyRole: ULong = 0UL
    var moderationDelay: Short = 250

    constructor()

    constructor(serverId: ULong) {
        this.serverId = serverId
    }

    fun writeTo(other: ServerSettingsRecord) {
        other.proxyRole = proxyRole
    }
}