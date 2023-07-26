/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a member's guild settings.
 *
 * Accessed with the `/systems/{sysid}/members/{memid}/guilds/{guildid}` route.
 *
 * Requires a token to access.
 *
 * @param displayName the display name of this member for this guild
 * @param avatarUrl the url for the avatar of this member for this guild
 * @param autoProxy whether autoproxy is enabled for this member for this guild
 * @param proxyEnabled whether proxying is enabled for this member for this guild
 * */
@Serializable
data class MemberGuildSettings(
    @SerialName("display_name")
    val displayName: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("auto_proxy")
    val autoProxy: Boolean,
    @SerialName("proxy_enabled")
    val proxyEnabled: Boolean
) {
    companion object {
        fun fromRecord(record: MemberServerSettingsRecord) = MemberGuildSettings(
            displayName = record.nickname,
            avatarUrl = record.avatarUrl,
            autoProxy = record.autoProxy,
            proxyEnabled = record.proxyEnabled
        )
    }
}
