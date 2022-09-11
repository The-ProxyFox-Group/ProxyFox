/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author Ampflower
 **/
class SystemRecord {
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var users: ArrayList<ULong> = ArrayList()
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null
    var timezone: String? = null
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null
    var autoType: AutoProxyMode = AutoProxyMode.OFF
    var trust: HashMap<ULong, TrustLevel> = HashMap()
}
