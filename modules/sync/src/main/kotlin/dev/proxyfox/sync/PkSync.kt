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
import dev.proxyfox.pluralkt.types.PkColor
import dev.proxyfox.pluralkt.types.PkError
import dev.proxyfox.pluralkt.types.PkMember
import dev.proxyfox.pluralkt.types.PkProxyTag

@OptIn(DontExpose::class)
object PkSync {
    sealed interface Either<A, B> {
        fun getA(): A?
        fun getB(): B?

        class EitherA<A, B>(private val a: A) : Either<A, B> {
            override fun getA() = a
            override fun getB() = null
        }

        class EitherB<A, B>(private val b: B) : Either<A, B> {
            override fun getA() = null
            override fun getB() = b
        }
    }

    private fun <T> Response<T>.getSuccessOrNull() = if (isSuccess()) getSuccess() else null

    suspend fun pull(system: SystemRecord): Either<Boolean, PkError> {
        val token = system.pkToken ?: return Either.EitherA(false)

        val systemResp = PluralKt.System.getMe(token).await()
        systemResp.getException().throwIfPresent()
        val pkSystem = systemResp.getSuccessOrNull() ?: return Either.EitherB(systemResp.getError())

        system.name = pkSystem.name ?: system.name
        system.description = pkSystem.description ?: system.description
        system.tag = pkSystem.tag ?: system.tag
        system.avatarUrl = pkSystem.avatarUrl ?: system.avatarUrl
        system.color = pkSystem.color?.color ?: system.color
        system.pronouns = pkSystem.pronouns ?: system.pronouns

        val membersResp = PluralKt.Member.getMembers(pkSystem.id, token).await()
        membersResp.getException().throwIfPresent()
        val pkMembers = membersResp.getSuccessOrNull() ?: return Either.EitherB(membersResp.getError())

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

        return Either.EitherA(true)
    }

    fun Array<PkMember>.getOrNull(id: String, name: String): PkMember? {
        for (member in this)
            if (member.id == id) return member
        for (member in this)
            if (member.name == name) return member
        return null
    }

    suspend fun push(system: SystemRecord): Either<Boolean, PkError> {
        val token = system.pkToken ?: return Either.EitherA(false)

        val systemResp = PluralKt.System.getMe(token).await()
        systemResp.getException().throwIfPresent()
        val pkSystem = systemResp.getSuccessOrNull() ?: return Either.EitherB(systemResp.getError())

        val memberResp = PluralKt.Member.getMembers(pkSystem.id, token).await()
        memberResp.getException().throwIfPresent()
        val pkMembers = memberResp.getSuccessOrNull() ?: return Either.EitherB(memberResp.getError())

        pkSystem.name = system.name
        pkSystem.description = system.description ?: pkSystem.description
        pkSystem.tag = system.tag ?: pkSystem.tag
        pkSystem.color = PkColor(system.color)
        pkSystem.pronouns = system.pronouns ?: pkSystem.pronouns

        val systemPushResp = PluralKt.System.updateSystem(pkSystem, token).await()
        systemPushResp.getException().throwIfPresent()
        if (systemPushResp.isError()) return Either.EitherB(systemPushResp.getError())

        val members = database.fetchMembersFromSystem(system.id)!!

        val memberToIdLookup = HashBiMap.create<PkMember, String>()
        val idToIdLookup = HashBiMap.create<String, String>()

        val newMems = ArrayList<PkMember>()

        for (member in members) {
            var new = false
            val pkMember = pkMembers.getOrNull(member.id, member.name) ?: let {
                new = true
                PkMember()
            }
            val proxies = database.fetchProxiesFromSystemAndMember(system.id, member.id)!!
            val pkProxies = ArrayList<PkProxyTag>()
            for (proxy in proxies) {
                val pkProxy = PkProxyTag()
                pkProxy.prefix = proxy.prefix
                pkProxy.suffix = proxy.suffix
                pkProxies.add(pkProxy)
            }

            if (
                pkMember.name == member.name &&
                pkMember.pronouns == member.pronouns &&
                pkMember.avatarUrl == member.avatarUrl &&
                pkMember.color == PkColor(member.color) &&
                pkMember.description == member.description &&
                pkMember.displayName == member.displayName &&
                pkMember.keepProxy == member.keepProxy &&
                pkMember.proxyTags == pkProxies
            ) continue

            pkMember.name = member.name
            pkMember.pronouns = member.pronouns ?: pkMember.pronouns
            pkMember.avatarUrl = member.avatarUrl ?: pkMember.avatarUrl
            pkMember.color = PkColor(member.color)
            pkMember.description = member.description ?: pkMember.description
            pkMember.displayName = member.displayName ?: pkMember.displayName
            pkMember.keepProxy = member.keepProxy
            pkMember.proxyTags = pkProxies

            val newMem = if (new) {
                PluralKt.Member.createMember(pkMember, token)
            } else {
                PluralKt.Member.updateMember(pkMember.id, pkMember, token)
            }.await().getSuccessOrNull() ?: continue

            newMems.add(newMem)
            memberToIdLookup[newMem] = member.id
            idToIdLookup[newMem.id] = member.id
        }

        // TODO: groups once implemented

        return Either.EitherA(true)
    }
}
