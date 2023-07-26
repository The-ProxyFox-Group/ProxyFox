/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

import dev.proxyfox.database.PkId
import dev.proxyfox.database.records.MongoRecord
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
class TokenRecord : MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()
    var id: PkId
    var token: String
    var systemId: PkId
    var type: TokenType

    constructor(token: String, id: PkId, systemId: PkId, type: TokenType) {
        this.token = token
        this.id = id
        this.systemId = systemId
        this.type = type
    }
}
