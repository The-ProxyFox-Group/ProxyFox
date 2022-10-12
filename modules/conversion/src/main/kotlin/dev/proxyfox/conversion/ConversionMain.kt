/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.conversion

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import dev.proxyfox.database.Database
import dev.proxyfox.database.databaseFromString
import dev.proxyfox.database.gson
import dev.proxyfox.importer.PluralKitImporter
import dev.proxyfox.types.PkSystem
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Converter")

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
        if (!file.exists() || !file.isFile) {
            exit(1, "File $file is invalid. Make sure you've selected the correct file. Example: `-from=legacy:legacy.json`")
        }

        logger.warn("Converting legacy database... this may take a while.")

        outputDatabase.setup().use { output ->
            JsonReader(file.reader()).use {
                val sysAdaptor = gson.getAdapter(PkSystem::class.java)
                it.beginObject()
                while (it.peek() != JsonToken.END_OBJECT) {
                    val id = it.nextName().toULong()
                    val obj = sysAdaptor.read(it) as PkSystem
                    try {
                        output.bulk {
                            val pki = object : PluralKitImporter(directAllocation = true, ignoreUnfinished = true) {}
                            pki.import(this, obj, id)
                            logger.info("Successfully imported {} -> {} with {} members", id, pki.getSystem().id, pki.getNewMembers())
                        }
                        if (output.fetchMembersFromUser(id)?.size != obj.members?.size) throw AssertionError("Size mismatch")
                    } catch (e: Exception) {
                        throw Exception("Failed with $id -> $obj", e)
                    }
                }
                it.endObject()
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