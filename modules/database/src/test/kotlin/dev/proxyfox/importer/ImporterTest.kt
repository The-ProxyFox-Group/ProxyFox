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
import dev.proxyfox.database.DatabaseTestUtil.instantEpoch
import dev.proxyfox.database.DatabaseTestUtil.instantLastMicroOfEpochDay
import dev.proxyfox.database.DatabaseTestUtil.seeded
import dev.proxyfox.database.InMemoryDatabase
import dev.proxyfox.database.MongoDatabase
import dev.proxyfox.database.etc.importer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.slf4j.LoggerFactory
import org.testng.Assert.*
import org.testng.annotations.*
import java.io.Reader
import java.net.URL
import java.nio.file.Files
import java.time.LocalDate

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
    private val prng = seeded()

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
        val user = entity<UserBehavior>(prng.nextLong().toULong())
        assertNull(database.fetchSystemFromUser(user), "$user already has system bound?")

        val importer1 = import(database, url.readText(), user)
        assertEquals(importer1.updatedMembers, 0, "Somehow updated existing member")

        assertNotNull(database.fetchMemberFromUserAndName(user, "Azalea"), "No such Azalea for $user")

        if (!url.file.contains("Tupperbox")) {
            assertEquals("| flwr", database.fetchSystemFromUser(user)?.tag, "Tag didn't get imported correctly.")
        }

        extraResource("PluralKit-v1-Case-Sensitivity-Test.json") {
            val pkImporter = import(database, it, user)
            assertEquals(pkImporter.createdMembers, 1, "`azalea` was not counted.")
        }

        assertEquals(database.fetchMemberFromUserAndName(user, "azalea")?.name, "azalea")

        database.dropSystem(user)
        database.getOrCreateSystem(user)

        val importer2 = import(database, url.readText(), user)
        assertEquals(importer2.updatedMembers, 0, "Somehow updated existing member")
        assertEquals(importer2.createdMembers, importer1.createdMembers, "Unexpected behaviour change")

        database.dropSystem(user)
        val id = database.getOrCreateSystem(user).id
        database.getOrCreateMember(id, "Azalea")

        val importer3 = import(database, url.readText(), user)
        assertEquals(importer3.updatedMembers, 1, "Updated more than Azalea")
        assertEquals(importer3.createdMembers, importer1.createdMembers - 1, "Unexpected behaviour change")

        // Somehow the ID manages to get reused in some implementations
        database.dropSystem(user)
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
        assertEquals(sorted[0].timestamp, instantEpoch)
        assertEquals(sorted[1].timestamp, instantLastMicroOfEpochDay)
    }

    @Test
    fun `Importer - switch dedup`() = runTest {
        val user = entity<UserBehavior>(16876UL)
        extraResource("PluralKit-v1-Switches-Dedup.json") {
            import(database, it, user)
        }

        val switches = database.fetchSwitchesFromUser(user)!!.sortedBy { it.timestamp }
        assertEquals(switches.size, 4, "Extra data than expected.")

        val member0 = database.fetchMemberFromUserAndName(user, "Azalea")!!.id
        val member1 = database.fetchMemberFromUserAndName(user, "Flora")!!.id

        assertEquals(switches[0].memberIds, listOf(member0))
        assertEquals(switches[1].memberIds, listOf(member0, member1))
        assertEquals(switches[2].memberIds, listOf(member1, member0))
        assertEquals(switches[3].memberIds, emptyList<String>())

        extraResource("PluralKit-v1-Switches-Dedup.json") {
            import(database, it, user)
        }

        val import2Switches = database.fetchSwitchesFromUser(user)?.sortedBy { it.timestamp }
        logger.info("{}", switches.joinToString("\n"))
        logger.info("{}", import2Switches?.joinToString("\n"))
        logger.info("{}", HashSet(import2Switches).also { it.removeAll(switches) }.joinToString("\n"))
        assertEquals(import2Switches, switches, "Switch data has been duplicated on reimport")
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
        private val logger = LoggerFactory.getLogger(ImporterTest::class.java)
        private const val path = "dev/proxyfox/database/systems"
        private val test = Files.createTempDirectory("ProxyFox-")
        private val resources = ClassPath.from(ImporterTest::class.java.classLoader).resources.filter { it.resourceName.startsWith(path) }

        @DataProvider
        @JvmStatic
        fun constructorParameters() = arrayOf(
            arrayOf("InMemory", { InMemoryDatabase() }),
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