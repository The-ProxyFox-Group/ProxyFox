/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.member

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import dev.proxyfox.database.PkId
import dev.proxyfox.database.records.MongoRecord
import org.bson.types.ObjectId

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author Ampflower
 **/
class MemberProxyTagRecord : MongoRecord {
    override var _id: ObjectId = ObjectId()

    // GSON-specific annotation for JSON database
    @Expose(serialize = false, deserialize = false)
    var systemId: PkId = ""

    // GSON-specific annotation for JSON database
    @SerializedName(value = "memberId", alternate = ["member"])
    var memberId: PkId = ""

    var prefix: String? = null
    var suffix: String? = null

    constructor()

    constructor(systemId: PkId, memberId: PkId, prefix: String?, suffix: String?) {
        this.systemId = systemId
        this.memberId = memberId
        this.prefix = if (prefix == "") null else prefix
        this.suffix = if (suffix == "") null else suffix
    }

    fun isEqual(other: MemberProxyTagRecord) = this === other || isEqual(prefix = other.prefix, suffix = other.suffix)

    fun isEqual(prefix: String?, suffix: String?): Boolean {
        return (this.prefix == prefix || (this.prefix.isNullOrEmpty() && prefix.isNullOrEmpty())) &&
                (this.suffix == suffix || (this.suffix.isNullOrEmpty() && suffix.isNullOrEmpty()))
    }

    fun test(message: String): Boolean {
        val pre = prefix == null || message.startsWith(prefix!!) || (suffix.isNullOrEmpty() && message == prefix!!.trimEnd())
        val suf = suffix == null || message.endsWith(suffix!!) || (prefix.isNullOrEmpty() && message == suffix!!.trimStart())
        return pre && suf
    }

    fun trim(message: String): String {
        val pLength = prefix?.length ?: 0
        val slength = suffix?.length ?: 0
        if (message.length < pLength + slength) {
            return ""
        }
        return message.substring(pLength, message.length - slength)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (
                other is MemberProxyTagRecord &&
                        other.memberId == memberId &&
                        other.systemId == systemId &&
                        isEqual(other)
                )
    }

    override fun hashCode(): Int {
        var result = systemId.hashCode()
        result = 31 * result + memberId.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + (suffix?.hashCode() ?: 0)
        return result
    }

    override fun toString() = "${prefix ?: ""}text${suffix ?: ""}"
}
