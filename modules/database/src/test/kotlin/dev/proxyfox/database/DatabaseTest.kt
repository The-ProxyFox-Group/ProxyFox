/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.kord.core.entity.Entity
import dev.proxyfox.database.DatabaseTestUtil.entity
import dev.proxyfox.database.DatabaseTestUtil.pkIdStream
import dev.proxyfox.database.DatabaseTestUtil.seeded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.testng.Assert.*
import org.testng.annotations.*
import java.nio.file.Files

// Created 2022-15-09T07:12:29

/**
 * @author Ampflower
 * @since ${version}
 **/
@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseTest @Factory(dataProvider = "constructorParameters") constructor(private val name: String, databaseFactory: () -> Database) {
    private val backing = lazy(databaseFactory)
    private val database: Database by backing

    @BeforeClass
    @Test(groups = ["construct"])
    fun construct() = runTest {
        assertNotNull(database)
    }

    //<editor-fold desc="Presetup - Expect failed lookups">
    @Test(
        groups = ["presetup"], dependsOnGroups = ["construct"], expectedExceptions = [UninitializedPropertyAccessException::class],
        dataProvider = "failPresetup3i"
    )
    fun <I1, I2, I3> `Presetup(I1, I2, I3) - expect fail`(i1: I1, i2: I2, i3: I3, func: suspend Database.(I1, I2, I3) -> Any?) = runTest {
        database.func(i1, i2, i3)
    }

    @Test(
        groups = ["presetup"], dependsOnGroups = ["construct"], expectedExceptions = [UninitializedPropertyAccessException::class],
        dataProvider = "failPresetup2i"
    )
    fun <I1, I2> `Presetup(I1, I2) - expect fail`(i1: I1, i2: I2, func: suspend Database.(I1, I2) -> Any?) = runTest {
        database.func(i1, i2)
    }

    @Test(
        groups = ["presetup"], dependsOnGroups = ["construct"], expectedExceptions = [UninitializedPropertyAccessException::class],
        dataProvider = "failPresetup1i"
    )
    fun <I1> `Presetup(I1) - expect fail`(i1: I1, func: suspend Database.(I1) -> Any?) = runTest {
        database.func(i1)
    }
    //</editor-fold>

    @Test(groups = ["setup"], dependsOnGroups = ["presetup"])
    fun setup() = runTest {
        database.setup()
    }

    //<editor-fold desc="Sanity-check empty stage">
    @Test(groups = ["empty"], dependsOnGroups = ["setup"], dataProvider = "allUsers")
    fun `Empty - null user fetch by ID`(user: ULong) = runTest {
        assertNull(database.fetchUser(user))
    }

    @Test(groups = ["empty"], dependsOnGroups = ["setup"], dataProvider = "allUsers")
    fun `Empty - null user fetch by entity`(user: ULong) = runTest {
        assertNull(database.fetchUser(entity(user)))
    }
    //</editor-fold>

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

        return "Database tester ${hashCode()} for $name ($backing)"
    }

    companion object {
        private const val userId1 = 196188877885538304L
        private const val userId2 = 822926373064671252L
        private const val userId3 = 561383325189668879L
        private const val userId4 = 415227941408997381L
        private const val userId5 = 462348944173957120L
        private val test = Files.createTempDirectory("ProxyFox-")


        init {
            println("Temporary folder @ $test")
        }

        @DataProvider
        @JvmStatic
        fun failPresetup3i() = arrayOf(
            withObject(0UL, 0UL, "aaaaa", Database::fetchMemberServerSettingsFromUserAndMember),
            withEntity(0UL, 0UL, "aaaaa", Database::fetchMemberServerSettingsFromUserAndMember),

            withObject(0UL, "aaaaa", "aaaaa", Database::fetchMemberServerSettingsFromSystemAndMember),
            withEntity(0UL, "aaaaa", "aaaaa", Database::fetchMemberServerSettingsFromSystemAndMember),
        )

        @DataProvider
        @JvmStatic
        fun failPresetup2i() = runBlocking {
            arrayOf(
                withObject(0UL, "aaaaa", Database::fetchMemberFromUser),
                withObject(0UL, "aaaaa", Database::fetchProxiesFromUserAndMember),

                // Not really member lookups
                withObject(0UL, "", Database::fetchMemberFromMessage),
                withObject(0UL, "", Database::fetchProxyTagFromMessage),

                withEntity(0UL, "aaaaa", Database::fetchMemberFromUser),
                withEntity(0UL, "aaaaa", Database::fetchProxiesFromUserAndMember),

                // Not really member lookups
                withEntity(0UL, "", Database::fetchMemberFromMessage),
                withEntity(0UL, "", Database::fetchProxyTagFromMessage),

                withObject("aaaaa", "aaaaa", Database::fetchMemberFromSystem),
                withObject("aaaaa", "aaaaa", Database::fetchProxiesFromSystemAndMember),
            )
        }

        @DataProvider
        @JvmStatic
        fun failPresetup1i() = arrayOf(
            withObject(0UL, Database::fetchUser),
            withObject(0UL, Database::getOrCreateUser),
            withObject(0UL, Database::fetchSystemFromUser),
            withObject(0UL, Database::fetchMembersFromUser),
            withObject(0UL, Database::fetchFrontingMembersFromUser),
            withObject(0UL, Database::fetchProxiesFromUser),

            withEntity(0UL, Database::fetchUser),
            withEntity(0UL, Database::getOrCreateUser),
            withEntity(0UL, Database::fetchSystemFromUser),
            withEntity(0UL, Database::fetchMembersFromUser),
            withEntity(0UL, Database::fetchFrontingMembersFromUser),
            withEntity(0UL, Database::fetchProxiesFromUser),

            withObject("aaaaa", Database::fetchSystemFromId),
            withObject("aaaaa", Database::fetchMembersFromSystem),
            withObject("aaaaa", Database::fetchFrontingMembersFromSystem),
            withObject("aaaaa", Database::fetchProxiesFromSystem),
        )

        @DataProvider
        @JvmStatic
        fun `16usersX16pkIds`(): Iterator<Array<Any>> = seeded().longs(16).boxed().flatMap { sys -> pkIdStream(16).mapToObj { mem -> arrayOf<Any>(sys, mem.toPkString()) } }.iterator()

        @DataProvider
        @JvmStatic
        fun `16x16pkIds`(): Iterator<Array<Any>> = pkIdStream(16).mapToObj(Int::toPkString).flatMap { sys -> pkIdStream(16).mapToObj { mem -> arrayOf<Any>(sys, mem.toPkString()) } }.iterator()

        @DataProvider
        @JvmStatic
        fun constructorParameters() = arrayOf(
            arrayOf("InMemory", { InMemoryDatabase() }),
            arrayOf("MongoDB", { MongoDatabase("TestFoxy-" + System.nanoTime()) }),
        )

        @DataProvider
        @JvmStatic
        fun allUsers() = arrayOf<Array<Any>>(
            arrayOf(userId1),
            arrayOf(userId2),
            arrayOf(userId3),
            arrayOf(userId4),
            arrayOf(userId5),
        )

        @AfterSuite
        @JvmStatic
        fun cleanupSuite() = Files.deleteIfExists(test)

        private fun <I1> withObject(i1: I1, func: suspend Database.(I1) -> Any?) = arrayOf(i1, func)

        private inline fun <reified I1 : Entity> withEntity(i1: ULong, noinline func: suspend Database.(I1) -> Any?) = arrayOf(entity<I1>(i1), func)

        private fun <I1, I2> withObject(i1: I1, i2: I2, func: suspend Database.(I1, I2) -> Any?) = arrayOf(i1, i2, func)

        private inline fun <reified I1 : Entity, I2> withEntity(i1: ULong, i2: I2, noinline func: suspend Database.(I1, I2) -> Any?) = arrayOf(entity<I1>(i1), i2, func)

        private fun <I1, I2, I3> withObject(i1: I1, i2: I2, i3: I3, func: suspend Database.(I1, I2, I3) -> Any?) = arrayOf(i1, i2, i3, func)

        private inline fun <reified I1 : Entity, I2, I3> withEntity(i1: ULong, i2: I2, i3: I3, noinline func: suspend Database.(I1, I2, I3) -> Any?) = arrayOf(entity<I1>(i1), i2, i3, func)

        private inline fun <reified I1 : Entity, reified I2 : Entity, I3> withEntity(i1: ULong, i2: ULong, i3: I3, noinline func: suspend Database.(I1, I2, I3) -> Any?) = arrayOf(entity<I1>(i1), entity<I2>(i2), i3, func)
    }
}