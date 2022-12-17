/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

import dev.proxyfox.database.*
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.Record
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
open class UserRecord : Record {
    var id: ULong = 0UL
    var systemId: PkId? = null

    override fun toMongo() = MongoUserRecord(this)
}

@Serializable
class MongoUserRecord : UserRecord, MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()

    constructor(record: UserRecord) {
        this.id = record.id
        this.systemId = record.systemId
    }

    override fun toMongo() = this
}
