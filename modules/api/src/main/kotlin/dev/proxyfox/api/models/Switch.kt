/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a switch.
 *
 * Accessed via the `/systems/{sysid}/switches` or `/system/{sysid}/fronters` routes.
 *
 * Requires a token to access.
 *
 * @param id the Pk-formatted ID of the switch
 * @param members the Pk-formatted IDs of the members in this switch
 * @param timestamp the timestamp of switch creation
 * */
@Serializable
data class Switch(
    val id: PkId,
    val members: List<PkId>,
    val timestamp: Instant
) {
    companion object {
        fun fromRecord(record: SystemSwitchRecord) = Switch(
            id = record.id,
            members = record.memberIds,
            timestamp = record.timestamp
        )
    }
}
