/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.conversion

import dev.proxyfox.database.Database
import dev.proxyfox.database.databaseFromString
import dev.proxyfox.database.etc.importer.PluralKitImporter
import dev.proxyfox.database.etc.types.PkSystem
import dev.proxyfox.database.records.misc.ServerSettingsRecord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Converter")

@OptIn(ExperimentalSerializationApi::class)
suspend fun main(args: Array<String>) {
    var from: String? = null
    var to: String? = null

    for (arg in args) {
        if (arg.startsWith("-from="))
            from = arg.substring(6)
        else if (arg.startsWith("-to="))
            to = arg.substring(4)
    }

    if (from == to) {
        exit(1, "Same database specified, exiting.")
    }

    val param = from ?: exit(1, "From not specified.")

    val outputDatabase = databaseFromString(to)

    if (param.startsWith("legacy")) {
        if (param.length <= 7) {
            exit(1, "Please select a file. Example: `-from=legacy:legacy.json`")
        }
        val file = File(param.substring(7))
        if (!file.exists()) {
            exit(1, "File $file is invalid. Make sure you've selected the correct file. Example: `-from=legacy:legacy.json`")
        }

        val systems = if (file.isFile) file else file.resolve("systems.json")
        val roles = (if (file.isFile) file.parentFile else file).resolve("roles.json")

        if (!systems.exists() || !roles.exists()) exit(1, "No data to import at $file; does `systems.json` or `roles.json` exist?")

        logger.info("Converting legacy database... this may take a while.")

        outputDatabase.setup().use { output ->
            if (systems.exists()) {
                logger.info("Importing systems.json...")

                systems.inputStream().use { input ->
                    Json.decodeFromStream<Map<ULong, PkSystem>>(input).forEach { (id, obj) ->
                        try {
                            output.bulk {
                                val pki = object : PluralKitImporter(directAllocation = true, ignoreUnfinished = true) {}
                                pki.import(this, obj, id)
                                logger.info("Successfully imported {} -> {} with {} members", id, pki.system.id, pki.createdMembers)
                            }
                            val importSize = obj.members?.size
                            val newSize = output.fetchMembersFromUser(id)?.size
                            if (importSize != newSize) logger.warn("Size mismatch for {}: {} -> {}", id, importSize, newSize)
                        } catch (e: Exception) {
                            throw Exception("Failed with $id -> $obj", e)
                        }
                    }
                }
            }

            if (roles.exists()) {
                logger.info("Importing roles.json...")
                output.bulk {
                    roles.inputStream().use { input ->
                        Json.decodeFromStream<Map<ULong, ULong>>(input).forEach { (id, role) ->
                            createServerSettings(ServerSettingsRecord().apply {
                                serverId = id
                                proxyRole = role
                            })
                        }
                    }
                }
            }
        }

        logger.info("Completed import.")

        exitProcess(0)
    }

    val inputDatabase: Database = databaseFromString(from)

    if (inputDatabase::class == outputDatabase::class) {
        exit(1, "Same database class specified, exiting.")
    }

    inputDatabase.setup().use { input ->
        outputDatabase.setup().use { output ->
            input.export(output)
        }
    }

    exitProcess(0)
}

private fun exit(exit: Int = 0, reason: String? = null): Nothing {
    reason?.let(logger::error)
    exitProcess(exit)
}