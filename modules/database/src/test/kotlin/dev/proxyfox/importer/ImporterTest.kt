/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.common.reflect.ClassPath
import dev.proxyfox.database.Database
import dev.proxyfox.database.DatabaseTestUtil.entity
import dev.proxyfox.database.JsonDatabase
import dev.proxyfox.database.MongoDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.testng.Assert.assertNotNull
import org.testng.annotations.*
import java.net.URL
import java.nio.file.Files

// Created 2022-29-09T22:17:51

/**
 * @author Ampflower
 * @since ${version}
 **/
@OptIn(ExperimentalCoroutinesApi::class)
class ImporterTest @Factory(dataProvider = "constructorParameters")
constructor(private val name: String, databaseFactory: () -> Database) {
    private val backing = lazy(databaseFactory)
    private val database: Database by backing

    @BeforeClass
    fun construct() = runTest {
        assertNotNull(database)
        database.setup()
    }

    @Test(expectedExceptions = [ImporterException::class], dataProvider = "failImporters")
    fun `Importer - expect fail`(url: URL) = runTest {
        import(database, url.readText(), entity(0UL))
    }

    @Test(dataProvider = "passImporters")
    fun `Importer - expect pass`(url: URL) = runTest {
        import(database, url.readText(), entity(0UL))
    }

    @Suppress("DEPRECATION_ERROR")
    @AfterClass
    fun cleanup() = runTest {
        if (backing.isInitialized()) {
            println("Dropping database $database")
            database.drop()
        } else {
            println("$name (${hashCode()}) not initialised, nothing to do.")
        }
    }

    override fun toString(): String {
        return "Importer Test ${hashCode()} for $name ($backing)"
    }

    companion object {
        private const val path = "dev/proxyfox/database/systems"
        private val test = Files.createTempDirectory("ProxyFox-")
        private val resources = ClassPath.from(ImporterTest::class.java.classLoader).resources.filter { it.resourceName.startsWith(path) }

        @DataProvider
        @JvmStatic
        fun constructorParameters() = arrayOf(
            arrayOf("JSON", { JsonDatabase(test.resolve("systems-${System.nanoTime()}.json").toFile()) }),
            arrayOf("MongoDB", { MongoDatabase("TestFoxy-" + System.nanoTime()) }),
        )

        @DataProvider
        @JvmStatic
        fun failImporters() = resources.filter { it.resourceName.startsWith("$path/fail/") }.map { it.url() }.iterator()

        @DataProvider
        @JvmStatic
        fun passImporters() = resources.filter { it.resourceName.startsWith("$path/pass/") }.map { it.url() }.iterator()

        @AfterSuite
        @JvmStatic
        fun cleanupSuite() = Files.deleteIfExists(test)
    }
}