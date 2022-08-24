package dev.proxyfox.conversion

import dev.proxyfox.database.Database
import dev.proxyfox.database.DatabaseUtil.databaseFromString
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

    inputDatabase.setup()
    outputDatabase.setup()

    inputDatabase.export(outputDatabase)

    exitProcess(0)
}