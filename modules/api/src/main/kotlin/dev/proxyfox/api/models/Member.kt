/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.common.fromColor
import dev.proxyfox.database.PkId
import dev.proxyfox.database.database
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.etc.ktx.serializaton.LocalDateLongMillisecondSerializer
import dev.proxyfox.database.records.member.MemberRecord
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a member.
 *
 * Accessed with the `/system/{sysid}/members` or
 * `/system/{sysid}/members/{memid}` routes.
 *
 * Requires a token to access.
 *
 * @param id the Pk-compatible ID of the member
 * @param name the name of the member
 * @param displayName the display name of the member
 * @param description the description of the member
 * @param color the color of the member (in a hexadecimal RGB format)
 * @param avatarUrl the URL for the member's avatar
 * @param keepProxy whether the member keeps their proxy tags in proxied messages
 * @param autoProxy whether autoproxy is enabled for this member
 * @param messageCount the amount of messages this member has sent
 * @param created the timestamp of the member creation
 * @param birthday the member's birthday
 * @param age the age of the member
 * @param role the role of the member
 * @param proxyTags the member's proxy tags
 * */
@Serializable
data class Member(
    val id: PkId,
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
    val created: Instant,
    val birthday: LocalDate?,
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
            created = member.timestamp,
            birthday = member.birthday,
            age = member.age,
            role = member.role,
            proxyTags = runBlocking { database.fetchProxiesFromSystemAndMember(member.systemId, member.id)?.map(ProxyTag::fromRecord) ?: emptyList() }
        )
    }
}