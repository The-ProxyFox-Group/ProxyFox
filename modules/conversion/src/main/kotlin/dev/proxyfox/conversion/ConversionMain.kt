package dev.proxyfox.conversion

import dev.proxyfox.database.Database
import dev.proxyfox.database.JsonDatabase
import dev.proxyfox.database.MongoDatabase
import dev.proxyfox.database.NopDatabase
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

    val inputDatabase: Database = when (from) {
        "json" -> JsonDatabase()
        "postgres" -> TODO("Postgres db isn't implemented yet!")
        "mongo" -> MongoDatabase()
        else -> NopDatabase()
    }
    inputDatabase.setup()
    val outputDatabase: Database = when (to) {
        "json" -> JsonDatabase()
        "postgres" -> TODO("Postgres db isn't implemented yet!")
        "mongo" -> MongoDatabase()
        else -> NopDatabase()
    }
    outputDatabase.setup()

    inputDatabase.export(outputDatabase)

    exitProcess(0)
}