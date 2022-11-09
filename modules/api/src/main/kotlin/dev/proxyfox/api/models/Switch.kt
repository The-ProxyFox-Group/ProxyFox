/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.serialization.Serializable

@Serializable
data class Switch(
    val id: String,
    val members: List<String>,
    val timestamp: String
) {
    companion object {
        fun fromRecord(record: SystemSwitchRecord) = Switch(
            id = record.id,
            members = record.memberIds,
            timestamp = record.timestamp.toString()
        )
    }
}
