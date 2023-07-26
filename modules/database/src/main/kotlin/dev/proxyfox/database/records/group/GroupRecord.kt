/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.group

import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.records.MongoRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

class GroupRecord() : MongoRecord {
    constructor(id: PkId, systemId: PkId, name: String) : this() {
        this.id = id
        this.systemId = systemId
        this.name = name
    }

    override var _id: ObjectId = ObjectId()

    var id: PkId = ""
    var systemId: PkId = ""
    var members: ArrayList<PkId> = arrayListOf()
    var name: String = ""
    var description: String? = null
    var color: Int = -1
    var avatarUrl: String? = null

    @Serializable(InstantLongMillisecondSerializer::class)
    var timestamp: Instant = Clock.System.now()
    var tag: String? = null
    var tagMode: TagMode = TagMode.HIDDEN
}