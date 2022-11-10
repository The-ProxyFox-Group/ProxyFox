/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import kotlinx.serialization.Serializable

@Serializable
data class SystemGuildSettings(val proxy_enabled: Boolean, val auto_proxy: String?, val auto_proxy_mode: AutoProxyMode) {
    companion object {
        fun fromRecord(record: SystemServerSettingsRecord) = SystemGuildSettings(
            proxy_enabled = record.proxyEnabled,
            auto_proxy = record.autoProxy,
            auto_proxy_mode = record.autoProxyMode
        )
    }
}
