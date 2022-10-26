/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import org.bson.types.ObjectId

// Created 2022-09-04T15:13:09

/**
 * A mutable record representing a system's server settings.
 *
 * @author Ampflower
 * @since ${version}
 **/
class SystemServerSettingsRecord : MongoRecord {
    override var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var systemId: String = ""
    var proxyEnabled: Boolean = true

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null
    var autoProxyMode: AutoProxyMode = AutoProxyMode.FALLBACK

    constructor()

    constructor(serverId: ULong, systemId: String) {
        this.serverId = serverId
        this.systemId = systemId
    }

    fun writeTo(other: SystemServerSettingsRecord, autoProxy: String?) {
        other.proxyEnabled = proxyEnabled
        other.autoProxy = autoProxy
        other.autoProxyMode = autoProxyMode
    }
}