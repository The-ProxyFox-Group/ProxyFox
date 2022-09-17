/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.proxyfox.common.printStep

lateinit var database: Database

suspend fun main(database: String?) = DatabaseMain.main(database)

object DatabaseMain {
    suspend fun main(db: String?) {
        printStep("Setup database", 1)
        database = try {
            databaseFromString(db)
        } catch (err: Throwable) {
            printStep("Database setup failed. Falling back to JSON", 2)
            JsonDatabase()
        }.setup()
        printStep("Registering shutdown hook for database", 2)
        // Allows the database to shut down & save correctly.
        Runtime.getRuntime().addShutdownHook(Thread(database::close))
    }
}