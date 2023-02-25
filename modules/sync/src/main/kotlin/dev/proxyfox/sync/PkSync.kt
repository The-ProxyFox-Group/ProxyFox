/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.sync

import com.google.common.collect.HashBiMap
import dev.proxyfox.common.annotations.DontExpose
import dev.proxyfox.common.throwIfPresent
import dev.proxyfox.database.database
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.pluralkt.PluralKt
import dev.proxyfox.pluralkt.Response
import dev.proxyfox.pluralkt.types.PkError
import dev.proxyfox.pluralkt.types.PkMember

object PkSync {
    private fun <T> Response<T>.getSuccessOrNull() = if (isSuccess()) getSuccess() else null

    @OptIn(DontExpose::class)
    suspend fun pull(system: SystemRecord): PkError? {
        val token = system.pkToken ?: return null

        val systemResp = PluralKt.System.getMe(token).await()
        systemResp.getException().throwIfPresent()
        val pkSystem = systemResp.getSuccessOrNull() ?: return systemResp.getError()

        system.name = pkSystem.name ?: system.name
        system.description = pkSystem.description ?: system.description
        system.tag = pkSystem.tag ?: system.tag
        system.avatarUrl = pkSystem.avatarUrl ?: system.avatarUrl
        system.color = pkSystem.color?.color ?: system.color
        system.pronouns = pkSystem.pronouns ?: system.pronouns

        val membersResp = PluralKt.Member.getMembers(pkSystem.id, token).await()
        membersResp.getException().throwIfPresent()
        val pkMembers = membersResp.getSuccessOrNull() ?: return membersResp.getError()

        val memberToIdLookup = HashBiMap.create<PkMember, String>()
        val idToIdLookup = HashBiMap.create<String, String>()

        for (pkMember in pkMembers) {
            val member = database.fetchMemberFromSystem(system.id, pkMember.id) ?: database.getOrCreateMember(
                system.id,
                pkMember.name
            )!!
            memberToIdLookup[pkMember] = member.id
            idToIdLookup[pkMember.id] = member.id

            member.name = pkMember.name
            member.pronouns = pkMember.pronouns
            member.avatarUrl = pkMember.avatarUrl
            member.color = pkMember.color?.color ?: member.color
            member.description = pkMember.description
            member.displayName = pkMember.displayName
            member.timestamp = pkMember.created ?: member.timestamp
            member.keepProxy = pkMember.keepProxy
            database.updateMember(member)

            val proxies = database.fetchProxiesFromSystemAndMember(system.id, member.id)!!

            for (pkProxy in pkMember.proxyTags) {
                var hasProxy = false
                for (proxy in proxies) {
                    if (proxy.prefix == pkProxy.prefix && proxy.suffix == pkProxy.suffix) {
                        hasProxy = true
                    }
                }
                if (!hasProxy) {
                    database.createProxyTag(system.id, member.id, pkProxy.prefix, pkProxy.suffix)
                }
            }
        }

        // TODO: groups once implemented

        return null
    }
}
