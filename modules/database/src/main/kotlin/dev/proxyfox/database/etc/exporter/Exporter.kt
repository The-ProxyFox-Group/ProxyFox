/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.exporter

import dev.proxyfox.database.Database
import dev.proxyfox.database.database
import dev.proxyfox.database.etc.types.*
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.TestOnly

object Exporter {
    suspend inline fun export(userId: ULong) = export(database, userId)

    suspend fun export(database: Database, userId: ULong): String {
        return exportToPkObject(database, userId)?.let { Json.Default.encodeToString(it) } ?: ""
    }

    @TestOnly
    suspend fun exportToPkObject(database: Database, userId: ULong): PkSystem? {
        val system = database.fetchSystemFromUser(userId) ?: return null
        val members = database.fetchMembersFromSystem(system.id)
        val memberIds = members?.mapTo(HashSet(), MemberRecord::id) ?: setOf()

        // Nested function for mapping switches to PkSwitch exports.
        fun toPkSwitch(record: SystemSwitchRecord): PkSwitch {
            // Note, ArrayList is used here to retain order.
            val existing = ArrayList(record.memberIds)

            // If retainAll modifies the list, take the slow route.
            if (existing.retainAll(memberIds)) {
                return PkSwitch(
                    timestamp = record.timestamp.toString(),
                    members = existing.toList(),

                    proxyfox = PfSwitchExtension(
                        allMembers = record.memberIds,
                    )
                )
            }

            return PkSwitch(record)
        }

        val pkSystem = PkSystem(
            system,
            members = members?.map {
                PkMember(it, database.fetchProxiesFromSystemAndMember(system.id, it.id)?.mapTo(HashSet(), ::PkProxy))
            },
            switches = database.fetchSwitchesFromSystem(system.id)?.map(::toPkSwitch),
        )
        return pkSystem
    }
}