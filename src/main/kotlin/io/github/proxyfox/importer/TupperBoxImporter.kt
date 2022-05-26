package io.github.proxyfox.importer

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.system.SystemRecord

/**
 * [Importer] to import a JSON with a TupperBox format
 *
 * @author Oliver
 * */
class TupperBoxImporter : Importer {
    override suspend fun import(string: String, userId: Snowflake) {
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