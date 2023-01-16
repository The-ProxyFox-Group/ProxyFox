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

@Serializable
data class SystemGuildSettings(
    @SerialName("proxy_enabled")
    val proxyEnabled: Boolean,
    @SerialName("auto_proxy")
    val autoProxy: String?,
    @SerialName("auto_proxy_mode")
    val autoProxyMode: AutoProxyMode
) {
    companion object {
        fun fromRecord(record: SystemServerSettingsRecord) = SystemGuildSettings(
            proxyEnabled = record.proxyEnabled,
            autoProxy = record.autoProxy,
            autoProxyMode = record.autoProxyMode
        )
    }
}
