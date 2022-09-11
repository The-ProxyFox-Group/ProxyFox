/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import dev.proxyfox.database.Database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord

/**
 * [Importer] to import a JSON with a TupperBox format
 *
 * @author Oliver
 * */
class TupperBoxImporter : Importer {
    override suspend fun import(database: Database, string: String, userId: ULong) {
        TODO("Not yet implemented")
    }

    // Getters:
    override suspend fun getSystem(): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getMembers(): List<MemberRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getNewMembers(): Int = 0

    override suspend fun getUpdatedMembers(): Int = 0
}