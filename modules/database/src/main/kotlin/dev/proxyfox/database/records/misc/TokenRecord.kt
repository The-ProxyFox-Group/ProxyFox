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

class TokenRecord : MongoRecord {
    override var _id: ObjectId = ObjectId()
    var token: String = ""
    var systemId: String = ""

    constructor()

    constructor(token: String, systemId: String) {
        this.token = token
        this.systemId = systemId
    }
}
