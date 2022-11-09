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
    val displayName: String?,
    val avatarUrl: String?,
    val autoProxy: Boolean,
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
