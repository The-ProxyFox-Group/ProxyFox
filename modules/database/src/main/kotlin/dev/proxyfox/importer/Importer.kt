package dev.proxyfox.importer

import com.google.gson.Gson
import dev.proxyfox.database.Database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import java.io.InputStreamReader

val gson = Gson()

/**
 * Imports a system file from a [String]. Supports both PluralKit and TupperBox formats.
 *
 * @param string The JSON to import
 * @return The importer used
 *
 * @author Oliver
 * */
suspend fun import(string: String, userId: String): Importer {
    val map = gson.fromJson(string, Map::class.java) as Map<String, *>
    val importer = if (map.containsKey("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(string, userId)
    return importer
}

/**
 * Imports a system file from an [InputStreamReader]. Supports both PluralKit and TupperBox formats.
 *
 * @param reader The JSON to import
 * @return The importer used
 *
 * @author Oliver
 * */
suspend fun import(reader: InputStreamReader, userId: String): Importer {
    val map = gson.fromJson(reader, Map::class.java)
    val importer = if (map.containsKey("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(reader.readText(), userId)
    return importer
}

/**
 * Imports a system file from a [String]. Supports both PluralKit and TupperBox formats.
 *
 * @param string The JSON to import
 * @return The importer used
 *
 * @author Oliver
 * */
suspend fun import(database: Database, string: String, userId: String): Importer {
    val map = gson.fromJson(string, Map::class.java) as Map<String, *>
    val importer = if (map.containsKey("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(database, string, userId)
    return importer
}

/**
 * Imports a system file from an [InputStreamReader]. Supports both PluralKit and TupperBox formats.
 *
 * @param reader The JSON to import
 * @return The importer used
 *
 * @author Oliver
 * */
suspend fun import(database: Database, reader: InputStreamReader, userId: String): Importer {
    val map = gson.fromJson(reader, Map::class.java)
    val importer = if (map.containsKey("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(database, reader.readText(), userId)
    return importer
}

/**
 * Interface to import a JSON file into the database
 *
 * @author Oliver
 * */
interface Importer {
    suspend fun import(string: String, userId: String)
    suspend fun import(database: Database, string: String, userId: String)

    // Getters:
    suspend fun getSystem(): SystemRecord
    suspend fun getMembers(): List<MemberRecord>
    suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord>
    suspend fun getNewMembers(): Int
    suspend fun getUpdatedMembers(): Int
}