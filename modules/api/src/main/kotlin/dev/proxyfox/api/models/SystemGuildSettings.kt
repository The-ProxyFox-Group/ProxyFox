/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a system's guild settings
 *
 * Accessed via the `/systems/{sysid}/guilds/{guildid}` route.
 *
 * Requires a token to access.
 *
 * @param proxyEnabled whether proxying is enabled in this guild
 * @param autoProxy the ID of the member that's currently being autoproxied (if autoProxyMode is not FALLBACK)
 * @param autoType the current mode of autoproxy
 * */
@Serializable
data class SystemGuildSettings(
    @SerialName("proxy_enabled")
    val proxyEnabled: Boolean,
    @SerialName("auto_proxy")
    val autoProxy: String?,
    @SerialName("auto_type")
    val autoType: AutoProxyMode
) {
    companion object {
        fun fromRecord(record: SystemServerSettingsRecord) = SystemGuildSettings(
            proxyEnabled = record.proxyEnabled,
            autoProxy = record.autoProxy,
            autoType = record.autoProxyMode
        )
    }
}
