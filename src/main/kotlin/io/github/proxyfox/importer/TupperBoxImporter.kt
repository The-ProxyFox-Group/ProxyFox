package io.github.proxyfox.importer

import io.github.proxyfox.database.*

/**
 * [Importer] to import a JSON with a TupperBox format
 *
 * @author Oliver
 * */
class TupperBoxImporter : Importer {
    override suspend fun import(map: Map<String, *>) {
        TODO("Not yet implemented")
    }

    override suspend fun finalizeImport() {
        TODO("Not yet implemented")
    }

    // Getters:
    override suspend fun getSystem(): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getMembers(): List<MemberRecord> {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberProxyTags(id: String): List<MemberProxyTagRecord> {
        TODO("Not yet implemented")
    }
}