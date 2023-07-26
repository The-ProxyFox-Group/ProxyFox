/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.importer

import dev.kord.core.entity.Entity
import dev.proxyfox.database.Database
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.InputStreamReader
import java.io.Reader

/**
 * Imports a system file from a [String]. Supports both PluralKit and TupperBox formats.
 *
 * @param string The JSON to import
 * @return The importer used
 * @throws ImporterException When importing fails for any reason
 * @author Oliver
 * */
suspend fun import(string: String, user: Entity?) = import(database, string, user)

/**
 * Imports a system file from an [InputStreamReader]. Supports both PluralKit and TupperBox formats.
 *
 * @param reader The JSON to import
 * @return The importer used
 * @throws ImporterException When importing fails for any reason
 * @author Oliver
 * */
suspend fun import(reader: Reader, user: Entity?) = import(database, reader, user)

/**
 * Imports a system file from a [String]. Supports both PluralKit and TupperBox formats.
 *
 * @param string The JSON to import
 * @return The importer used
 * @throws ImporterException When importing fails for any reason
 * @author Oliver
 * */
suspend fun import(database: Database, string: String, user: Entity?): Importer {
    try {
        println(string)
        return import(database, Json.parseToJsonElement(string), user)
    } catch (reason: Throwable) {
        throw ImporterException("Not a JSON file $reason", reason)
    }
}

/**
 * Imports a system file from an [InputStreamReader]. Supports both PluralKit and TupperBox formats.
 *
 * @param reader The JSON to import
 * @return The importer used
 * @throws ImporterException When importing fails for any reason
 * @author Oliver
 * */
suspend fun import(database: Database, reader: Reader, user: Entity?): Importer {
    return import(database, reader.readText(), user)
}

/**
 * Imports a system file from a [JsonElement]. Supports both PluralKit and TupperBox formats.
 *
 * @param element The JSON to import
 * @return The importer used
 * @throws ImporterException When importing fails for any reason
 * @author Oliver
 * */
suspend fun import(database: Database, element: JsonElement, user: Entity?): Importer {
     val map = element.jsonObject
    if (map.isEmpty()) throw ImporterException("No data to import.")
    if (map.contains("type") && map.contains("uri") && map.size == 2) {
        throw ImporterException("Your system file is invalid; try fetching directly from ${map["uri"]}?")
    }
    val importer = if (map.contains("tuppers")) TupperBoxImporter() else PluralKitImporter()
    val bulk = database.bulkInserter()
    importer.import(bulk, map, user!!.id.value)
    bulk.commit()
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
    fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord>

    val system: SystemRecord
    val members: List<MemberRecord>
    val createdMembers: Int
    val updatedMembers: Int
}