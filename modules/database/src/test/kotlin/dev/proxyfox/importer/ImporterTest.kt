/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.common.reflect.ClassPath
import dev.kord.core.behavior.UserBehavior
import dev.proxyfox.database.Database
import dev.proxyfox.database.DatabaseTestUtil.entity
import dev.proxyfox.database.DatabaseTestUtil.offsetDateTimeEpoch
import dev.proxyfox.database.DatabaseTestUtil.seeded
import dev.proxyfox.database.JsonDatabase
import dev.proxyfox.database.MongoDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.annotations.*
import java.io.Reader
import java.net.URL
import java.nio.file.Files
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
        val user = entity<UserBehavior>(seeded().nextLong().toULong())
        import(database, url.readText(), user)

        assertNotNull(database.fetchMemberFromUserAndName(user, "Azalea"), "No such Azalea for $user")
    }

    @Test
    fun `Importer - time tolerance A`() = runTest {
        val user = entity<UserBehavior>(32767UL)
        extraResource("ProxyFox-v1-Time-Tolerance-A.json") {
            import(database, it, user)
        }

        assertEquals(database.fetchMemberFromUserAndName(user, "Azalea")!!.birthday, LocalDate.of(1, 12, 25))
        assertEquals(database.fetchMemberFromUserAndName(user, "Berry")!!.birthday, LocalDate.of(1, 1, 2))
        assertEquals(database.fetchMemberFromUserAndName(user, "Cherry")!!.birthday, LocalDate.of(1, 4, 10))
        assertEquals(database.fetchMemberFromUserAndName(user, "Hibiscus")!!.birthday, LocalDate.of(1990, 7, 4))
        assertEquals(database.fetchMemberFromUserAndName(user, "Zinnia")!!.birthday, LocalDate.of(2000, 2, 4))
        assertEquals(database.fetchMemberFromUserAndName(user, "Ivy")!!.birthday, LocalDate.of(1995, 8, 24))
    }

    @Test
    fun `Importer - time tolerance B`() = runTest {
        val user = entity<UserBehavior>(65535UL)
        extraResource("ProxyFox-v1-Time-Tolerance-B.json") {
            import(database, it, user)
        }

        assertEquals(database.fetchMemberFromUserAndName(user, "Azalea")!!.birthday, LocalDate.of(1, 12, 25))
        assertEquals(database.fetchMemberFromUserAndName(user, "Berry")!!.birthday, LocalDate.of(1, 2, 1))
        assertEquals(database.fetchMemberFromUserAndName(user, "Cherry")!!.birthday, LocalDate.of(1, 10, 4))
        assertEquals(database.fetchMemberFromUserAndName(user, "Hibiscus")!!.birthday, LocalDate.of(1990, 4, 7))
        assertEquals(database.fetchMemberFromUserAndName(user, "Zinnia")!!.birthday, LocalDate.of(2000, 2, 4))
        assertEquals(database.fetchMemberFromUserAndName(user, "Ivy")!!.birthday, LocalDate.of(1995, 8, 24))
    }

    @Test
    fun `Importer - time resolution`() = runTest {
        val user = entity<UserBehavior>(2048UL)
        passResource("ProxyFox-v1-Time-Resolution.json") {
            import(database, it, user)
        }

        val switches = database.fetchSwitchesFromUser(user)
        assertNotNull(switches, "switches")
        val sorted = switches!!.sortedBy { it.timestamp }
        assertEquals(sorted[0].timestamp, offsetDateTimeEpoch)
        assertEquals(sorted[1].timestamp, OffsetDateTime.of(1970, 1, 1, 23, 59, 59, 999_999_999, ZoneOffset.UTC))
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

        private inline fun extraResource(resource: String, action: (Reader) -> Unit) {
            ImporterTest::class.java.getResourceAsStream("/$path/extra/$resource")!!.reader().use(action)
        }

        private inline fun passResource(resource: String, action: (Reader) -> Unit) {
            ImporterTest::class.java.getResourceAsStream("/$path/pass/$resource")!!.reader().use(action)
        }
    }
}