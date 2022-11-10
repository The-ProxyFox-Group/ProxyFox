/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import kotlinx.serialization.Serializable

@Serializable
data class MemberGuildSettings(
    val display_name: String?,
    val avatar_url: String?,
    val auto_proxy: Boolean,
    val proxy_enabled: Boolean
) {
    companion object {
        fun fromRecord(record: MemberServerSettingsRecord) = MemberGuildSettings(
            display_name = record.nickname,
            avatar_url = record.avatarUrl,
            auto_proxy = record.autoProxy,
            proxy_enabled = record.proxyEnabled
        )
    }
}
