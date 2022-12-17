/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.database.*
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.Record
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
open class SystemChannelSettingsRecord() : Record {
    var serverId: ULong = 0UL
    var channelId: ULong = 0UL
    var systemId: PkId = ""
    var proxyEnabled: Boolean = true

    constructor(channelId: ULong, systemId: PkId) : this() {
        this.channelId = channelId
        this.systemId = systemId
    }

    fun writeTo(other: SystemChannelSettingsRecord) {
        other.proxyEnabled = proxyEnabled
    }

    override fun toMongo() = MongoSystemChannelSettingsRecord(this)
}

@Serializable
class MongoSystemChannelSettingsRecord: SystemChannelSettingsRecord, MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()

    constructor(record: SystemChannelSettingsRecord) {
        serverId = record.serverId
        channelId = record.channelId
        systemId = record.systemId
        proxyEnabled = record.proxyEnabled
    }

    override fun toMongo() = this
}
