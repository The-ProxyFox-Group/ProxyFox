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
import dev.proxyfox.database.records.misc.AutoProxyMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T15:13:09

/**
 * A mutable record representing a system's server settings.
 *
 * @author Ampflower
 * @since ${version}
 **/
@Serializable
class SystemServerSettingsRecord() : MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var systemId: PkId = ""
    var proxyEnabled: Boolean = true

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: PkId? = null
    var autoProxyMode: AutoProxyMode = AutoProxyMode.FALLBACK

    constructor(serverId: ULong, systemId: PkId) : this() {
        this.serverId = serverId
        this.systemId = systemId
    }

    fun writeTo(other: SystemServerSettingsRecord, autoProxy: String?) {
        other.proxyEnabled = proxyEnabled
        other.autoProxy = autoProxy
        other.autoProxyMode = autoProxyMode
    }
}