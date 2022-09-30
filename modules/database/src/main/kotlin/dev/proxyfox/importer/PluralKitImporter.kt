/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.gson.JsonObject
import dev.proxyfox.common.toColor
import dev.proxyfox.database.Database
import dev.proxyfox.database.gson
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.sanitise
import dev.proxyfox.database.validate
import dev.proxyfox.types.PkSystem

/**
 * [Importer] to import a JSON with a PluralKit format
 *
 * @author Oliver
 * */
class PluralKitImporter : Importer {
    private lateinit var system: SystemRecord
    private var members: List<MemberRecord> = ArrayList()
    private var proxies: HashMap<MemberRecord, List<MemberProxyTagRecord>> = HashMap()
    private var createdMembers = 0
    private var updatedMembers = 0

    override suspend fun import(database: Database, json: JsonObject, userId: ULong) {
        val pkSystem = gson.fromJson(json, PkSystem::class.java)
        system = database.getOrCreateSystem(userId)
        system.name = pkSystem.name.sanitise() ?: system.name
        system.description = pkSystem.description.sanitise() ?: system.description
        system.tag = pkSystem.tag.sanitise() ?: system.tag
        system.avatarUrl = pkSystem.avatar_url.sanitise() ?: system.avatarUrl
        if (pkSystem.members != null)
            for (pkMember in pkSystem.members!!) {
                val member = database.fetchMemberFromSystemAndName(system.id, pkMember.name.validate("members/name"))?.apply { updatedMembers++ }
                    ?: database.getOrCreateMember(system.id, pkMember.name.validate("members/name"))?.apply { createdMembers++ }
                    ?: throw ImporterException("Database didn't create member")
                member.displayName = pkMember.display_name.sanitise() ?: member.displayName
                member.avatarUrl = pkMember.avatar_url.sanitise() ?: member.avatarUrl
                member.description = pkMember.description.sanitise() ?: member.description
                member.pronouns = pkMember.pronouns.sanitise() ?: member.pronouns
                member.color = pkMember.color?.validate("members[${pkMember.id} (${pkMember.name})]/color")?.toColor() ?: member.color
                member.keepProxy = pkMember.keep_proxy ?: member.keepProxy
                member.messageCount = pkMember.message_count ?: member.messageCount
                pkMember.proxy_tags?.forEach { pkProxy ->
                    database.createProxyTag(system.id, member.id, pkProxy.prefix, pkProxy.suffix)
                }
                database.updateMember(member)
            }
        database.updateSystem(system)
    }

    // Getters:
    override suspend fun getSystem(): SystemRecord = system

    override suspend fun getMembers(): List<MemberRecord> = members

    override suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> = proxies[member]!!

    override suspend fun getNewMembers(): Int = createdMembers

    override suspend fun getUpdatedMembers(): Int = updatedMembers
}

