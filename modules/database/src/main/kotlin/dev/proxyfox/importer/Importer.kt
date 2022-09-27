/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kord.core.entity.Entity
import dev.proxyfox.database.Database
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import java.io.InputStreamReader

/**
 * Imports a system file from a [String]. Supports both PluralKit and TupperBox formats.
 *
 * @param string The JSON to import
 * @return The importer used
 *
 * @author Oliver
 * */
suspend fun import(string: String, user: Entity?): Importer {
    val map = JsonParser.parseString(string).asJsonObject
    val importer = if (map.has("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(map, user!!.id.value)
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
suspend fun import(reader: InputStreamReader, user: Entity?): Importer {
    val map = JsonParser.parseReader(reader).asJsonObject
    val importer = if (map.has("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(map, user!!.id.value)
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
suspend fun import(database: Database, string: String, user: Entity?): Importer {
    val map = JsonParser.parseString(string).asJsonObject
    val importer = if (map.has("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(database, map, user!!.id.value)
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
suspend fun import(database: Database, reader: InputStreamReader, user: Entity?): Importer {
    val map = JsonParser.parseReader(reader).asJsonObject
    val importer = if (map.has("tuppers")) TupperBoxImporter() else PluralKitImporter()
    importer.import(database, map, user!!.id.value)
    return importer
}

/**
 * Interface to import a JSON file into the database
 *
 * @author Oliver
 * */
interface Importer {
    suspend fun import(json: JsonObject, userId: ULong) = import(database, json, userId)
    suspend fun import(database: Database, json: JsonObject, userId: ULong)

    // Getters:
    suspend fun getSystem(): SystemRecord
    suspend fun getMembers(): List<MemberRecord>
    suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord>
    suspend fun getNewMembers(): Int
    suspend fun getUpdatedMembers(): Int
}