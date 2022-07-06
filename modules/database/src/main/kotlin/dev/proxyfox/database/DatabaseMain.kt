package dev.proxyfox.database

import dev.proxyfox.common.printStep

lateinit var database: Database

suspend fun main() = DatabaseMain.main()

object DatabaseMain {
    suspend fun main() {
        printStep("Setup database", 1)
        database = try {
            val db = MongoDatabase()
            db.setup()
            db
        } catch (err: Throwable) {
            printStep("Database setup failed. Falling back to JSON", 2)
            val db = JsonDatabase()
            db.setup()
            db
        }
        printStep("Registering shutdown hook for database", 2)
        // Allows the database to shut down & save correctly.
        Runtime.getRuntime().addShutdownHook(Thread(database::close))
    }
}