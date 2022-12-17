/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

import dev.proxyfox.database.PkId
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.Record
import kotlinx.datetime.Clock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
open class ProxiedMessageRecord : Record {
    var creationDate = Clock.System.now()
    var memberName: String = ""
    var userId: ULong = 0UL
    var oldMessageId: ULong = 0UL
    var newMessageId: ULong = 0UL
    var guildId: ULong = 0UL
    var channelId: ULong = 0UL
    var threadId: ULong? = null
    var memberId: PkId = ""
    var systemId: PkId = ""
    var deleted = false

    override fun toMongo() = MongoProxiedMessageRecord(this)
}

@Serializable
class MongoProxiedMessageRecord : ProxiedMessageRecord, MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()

    constructor(record: ProxiedMessageRecord) {
        this.creationDate = record.creationDate
        this.memberName = record.memberName
        this.userId = record.userId
        this.oldMessageId = record.oldMessageId
        this.newMessageId = record.newMessageId
        this.guildId = record.guildId
        this.channelId = record.channelId
        this.threadId = record.threadId
        this.memberId = record.memberId
        this.systemId = record.systemId
        this.deleted = record.deleted
    }

    override fun toMongo() = this
}
