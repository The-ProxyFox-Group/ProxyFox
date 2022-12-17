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
import dev.proxyfox.database.*
import dev.proxyfox.database.records.MongoRecord
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

// Created 2022-09-04T15:17:43

/**
 * A mutable record representing a member's proxy tags.
 *
 * @author Ampflower
 **/
@Serializable
class MemberProxyTagRecord(): MongoRecord {
    @Contextual
    override var _id: ObjectId = ObjectId()

    var systemId: PkId = ""
    var memberId: PkId = ""

    var prefix: String? = null
    var suffix: String? = null

    constructor(systemId: PkId, memberId: PkId, prefix: String?, suffix: String?): this() {
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
        val pre = prefix == null || message.startsWith(prefix!!)
        val suf = suffix == null || message.endsWith(suffix!!)
        return pre && suf
    }

    fun trim(message: String): String {
        val pLength = prefix?.length ?: 0
        val slength = suffix?.length ?: 0
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
