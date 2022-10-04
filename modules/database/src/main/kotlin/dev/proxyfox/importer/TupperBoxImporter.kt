/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.gson.JsonObject
import dev.proxyfox.database.Database
import dev.proxyfox.database.gson
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.types.TbSystem

/**
 * [Importer] to import a JSON with a TupperBox format
 *
 * @author Oliver
 * */
class TupperBoxImporter : Importer {
    private lateinit var system: SystemRecord
    private var members: List<MemberRecord> = ArrayList()
    private var proxies = HashMap<MemberRecord, MemberProxyTagRecord>()
    private var createdMembers = 0
    private var updatedMembers = 0

    override suspend fun import(database: Database, json: JsonObject, userId: ULong) {
        val tbSystem = gson.fromJson(json, TbSystem::class.java)
        system = database.getOrCreateSystem(userId)

        tbSystem.tuppers?.let { tbMembers ->
            for (tbMember in tbMembers) {
                val member = database.fetchMemberFromSystemAndName(system.id, tbMember.name)?.apply { updatedMembers++ }
                    ?: database.getOrCreateMember(system.id, tbMember.name)?.apply { createdMembers++ }
                    ?: continue
                tbMember.applyTo(member)
                tbMember.brackets?.let {
                    if (it.size >= 2) {
                        proxies[member] = database.createProxyTag(member.systemId, member.id, it[0], it[1])!!
                    }
                }
                database.updateMember(member)
            }
        }
        // No need to update the system, as TupperBox doesn't have any globals.
    }

    // Getters:
    override suspend fun getSystem(): SystemRecord {
        return system
    }

    override suspend fun getMembers(): List<MemberRecord> {
        return members
    }

    override suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> {
        return listOfNotNull(proxies[member])
    }

    override suspend fun getNewMembers(): Int = createdMembers

    override suspend fun getUpdatedMembers(): Int = updatedMembers
}