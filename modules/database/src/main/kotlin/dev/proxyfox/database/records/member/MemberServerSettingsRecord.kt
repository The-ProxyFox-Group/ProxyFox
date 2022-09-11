/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import org.bson.types.ObjectId

// Created 2022-09-04T14:16:19

/**
 * A mutable record representing a member's server settings.
 *
 * @author KJP12
 **/
class MemberServerSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var systemId: String = ""
    var memberId: String = ""
    var avatarUrl: String? = null
    var nickname: String? = null

    /** Whether the member should be auto-proxied in the server. */
    var autoProxy: Boolean = false
    var proxyEnabled: Boolean = true
}