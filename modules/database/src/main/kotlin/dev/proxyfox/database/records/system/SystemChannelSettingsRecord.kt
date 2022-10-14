/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import org.bson.types.ObjectId

class SystemChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var channelId: ULong = 0UL
    var systemId: String = ""
    var proxyEnabled: Boolean = true

    constructor()

    constructor(
        channelId: ULong,
        systemId: String,
    ) {
        this.channelId = channelId
        this.systemId = systemId
    }

    fun writeTo(other: SystemChannelSettingsRecord) {
        other.proxyEnabled = proxyEnabled
    }
}
