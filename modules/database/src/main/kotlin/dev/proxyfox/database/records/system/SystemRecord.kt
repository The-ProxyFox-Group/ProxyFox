/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.system

import dev.proxyfox.common.annotations.DontExpose
import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author Ampflower
 **/
@Serializable
open class SystemRecord : MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()
    var id: PkId = ""
    var users: ArrayList<ULong> = ArrayList()
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null
    var timezone: String? = null

    @Serializable(InstantLongMillisecondSerializer::class)
    var timestamp: Instant = Clock.System.now()

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: PkId? = null
    var autoType: AutoProxyMode = AutoProxyMode.OFF
    var trust: HashMap<ULong, TrustLevel> = HashMap()

    @DontExpose("PluralKit Tokens grant access to edit systems in PK's API!")
    var pkToken: String? = null

    val showName get() = name?.let { "$it [`$id`]" } ?: "`$id`"

    fun canAccess(user: ULong): Boolean {
        if (users.contains(user)) return true
        val trust = trust[user] ?: return false
        return trust != TrustLevel.NONE
    }

    fun canEditSwitches(user: ULong): Boolean {
        if (users.contains(user)) return true
        val trust = trust[user] ?: return false
        if (trust == TrustLevel.SWITCH) return true
        return trust == TrustLevel.FULL
    }

    fun canEditMembers(user: ULong): Boolean {
        if (users.contains(user)) return true
        val trust = trust[user] ?: return false
        if (trust == TrustLevel.MEMBER) return true
        return trust == TrustLevel.FULL
    }

    fun hasFullAccess(user: ULong): Boolean {
        if (users.contains(user)) return true
        val trust = trust[user] ?: return false
        return trust == TrustLevel.FULL
    }
}
