/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.common.fromColor
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberRecord
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val name: String,
    val display_name: String?,
    val description: String?,
    val pronouns: String?,
    val color: String?,
    val avatar_url: String?,
    val keep_proxy: Boolean,
    val auto_proxy: Boolean,
    val message_count: ULong,
    val created: String,
    val birthday: String?,
    val age: String?,
    val role: String?,
    val proxy_tags : List<ProxyTag>
) {
    companion object {
        fun fromRecord(member: MemberRecord) = Member(
            id = member.id,
            name = member.name,
            display_name = member.displayName,
            description = member.description,
            pronouns = member.pronouns,
            color = member.color.fromColor(),
            avatar_url = member.avatarUrl,
            keep_proxy = member.keepProxy,
            auto_proxy = member.autoProxy,
            message_count = member.messageCount,
            created = member.timestamp.toString(),
            birthday = member.birthday.toString(),
            age = member.age,
            role = member.role,
            proxy_tags = runBlocking { database.fetchProxiesFromSystemAndMember(member.systemId, member.id)?.map(ProxyTag::fromRecord) ?: emptyList() }
        )
    }
}