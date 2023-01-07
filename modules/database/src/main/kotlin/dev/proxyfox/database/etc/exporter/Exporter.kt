/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.exporter

import dev.proxyfox.database.Database
import dev.proxyfox.database.database
import dev.proxyfox.database.etc.types.PkMember
import dev.proxyfox.database.etc.types.PkProxy
import dev.proxyfox.database.etc.types.PkSwitch
import dev.proxyfox.database.etc.types.PkSystem
import dev.proxyfox.database.gson

object Exporter {
    suspend inline fun export(userId: ULong) = export(database, userId)

    suspend fun export(database: Database, userId: ULong): String {
        val system = database.fetchSystemFromUser(userId) ?: return ""

        val pkSystem = PkSystem(
            system,
            members = database.fetchMembersFromSystem(system.id)?.map {
                PkMember(it, database.fetchProxiesFromSystemAndMember(system.id, it.id)?.mapTo(HashSet(), ::PkProxy))
            },
            switches = database.fetchSwitchesFromSystem(system.id)?.map(::PkSwitch),
        )
        return gson.toJson(pkSystem)
    }
}