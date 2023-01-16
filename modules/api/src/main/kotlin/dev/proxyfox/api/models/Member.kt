/*
 * Copyright (c) 2022-2023, The ProxyFox Group
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val name: String,
    @SerialName("display_name")
    val displayName: String?,
    val description: String?,
    val pronouns: String?,
    val color: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("keep_proxy")
    val keepProxy: Boolean,
    @SerialName("auto_proxy")
    val autoProxy: Boolean,
    @SerialName("message_count")
    val messageCount: ULong,
    val created: String,
    val birthday: String?,
    val age: String?,
    val role: String?,
    @SerialName("proxy_tags")
    val proxyTags : List<ProxyTag>
) {
    companion object {
        fun fromRecord(member: MemberRecord) = Member(
            id = member.id,
            name = member.name,
            displayName = member.displayName,
            description = member.description,
            pronouns = member.pronouns,
            color = member.color.fromColor(),
            avatarUrl = member.avatarUrl,
            keepProxy = member.keepProxy,
            autoProxy = member.autoProxy,
            messageCount = member.messageCount,
            created = member.timestamp.toString(),
            birthday = member.birthday.toString(),
            age = member.age,
            role = member.role,
            proxyTags = runBlocking { database.fetchProxiesFromSystemAndMember(member.systemId, member.id)?.map(ProxyTag::fromRecord) ?: emptyList() }
        )
    }
}