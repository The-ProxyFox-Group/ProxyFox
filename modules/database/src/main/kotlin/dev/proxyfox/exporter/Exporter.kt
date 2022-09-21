/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.exporter

import dev.proxyfox.common.fromColor
import dev.proxyfox.database.Database
import dev.proxyfox.database.database
import dev.proxyfox.importer.gson
import dev.proxyfox.types.PkMember
import dev.proxyfox.types.PkProxy
import dev.proxyfox.types.PkSystem

object Exporter {
    suspend inline fun export(userId: ULong) = export(database, userId)

    suspend fun export(database: Database, userId: ULong): String {
        val system = database.fetchSystemFromUser(userId) ?: return ""

        val pkSystem = PkSystem()
        pkSystem.name = system.name
        pkSystem.description = system.description
        pkSystem.tag = system.tag
        pkSystem.avatar_url = system.avatarUrl

        val members = database.fetchMembersFromSystem(system.id) ?: ArrayList()
        pkSystem.members = Array(members.size) {
            val member = members[it]
            val pkMember = PkMember()
            pkMember.name = member.name
            pkMember.display_name = member.displayName
            pkMember.description = member.description
            pkMember.pronouns = member.pronouns
            pkMember.color = member.color.fromColor()
            pkMember.keep_proxy = member.keepProxy
            pkMember.message_count = member.messageCount
            pkMember.avatar_url = member.avatarUrl

            val proxies = database.fetchProxiesFromSystemAndMember(system.id, member.id)
            val pkProxies = ArrayList<PkProxy>()
            if (proxies != null) {
                for (proxy in proxies) {
                    val pkProxy = PkProxy()
                    pkProxy.prefix = proxy.prefix
                    pkProxy.suffix = proxy.suffix
                    pkProxies.add(pkProxy)
                }
            }
            pkMember.proxy_tags = pkProxies.toTypedArray()

            pkMember
        }
        return gson.toJson(pkSystem)
    }
}