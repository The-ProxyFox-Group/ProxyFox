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
import kotlin.system.exitProcess

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
        println("Same database specified, exiting.")
        exitProcess(1)
    }

    val inputDatabase: Database = databaseFromString(from)
    val outputDatabase = databaseFromString(to)

    if (inputDatabase::class == outputDatabase::class) {
        println("Same database class specified, exiting.")
        exitProcess(1)
    }

    inputDatabase.setup().use { input ->
        outputDatabase.setup().use { output ->
            input.export(output)
        }
    }

    exitProcess(0)
}