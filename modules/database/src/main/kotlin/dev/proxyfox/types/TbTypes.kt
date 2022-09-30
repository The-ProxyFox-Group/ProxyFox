/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.types

import dev.proxyfox.database.records.member.MemberRecord
import java.time.OffsetDateTime

// Created 2022-29-09T22:20:20

/**
 * @author KJP12
 * @since ${version}
 **/
class TbSystem {
    var tuppers: List<TbMember>? = null
    var groups: List<TbGroup>? = null
}

class TbMember {
    var id: Int = 0
    var name: String = ""
    var avatar_url: String? = null
    var brackets: Array<String>? = null
    var show_brackets: Boolean = false
    var birthday: OffsetDateTime? = null
    var description: String? = null
    var tag: String? = null
    var group_id: Int? = null
    var nick: String? = null

    fun applyTo(record: MemberRecord) {
        record.name = name
        record.keepProxy = show_brackets

        avatar_url?.let { record.avatarUrl = it }
        nick?.let { record.displayName = it }
        birthday?.let { record.birthday = it.toString() }
        description?.let { record.description = it }
    }
}

class TbGroup {
    var id: Int = 0
    var name: String = ""
    var description: String? = null
    var tag: String? = null
}