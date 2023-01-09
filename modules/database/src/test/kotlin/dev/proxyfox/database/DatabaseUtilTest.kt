/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.proxyfox.database.DatabaseTestUtil.pkIdStream
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

// Created 2022-22-09T01:35:21

/**
 * @author Ampflower
 * @since ${version}
 **/
class DatabaseUtilTest {
    @Test(dataProvider = "randomIds")
    fun `#fromPkString(randomIds)`(input: String, expected: Int) {
        assertEquals(input.fromPkString(), expected, "$input -> ${input.fromPkString().toPkString()} |")
    }

    @Test(dataProvider = "knownIds")
    fun `#fromPkString(knownIds)`(input: String, expected: Int) {
        assertEquals(input.fromPkString(), expected, "$input -> ${input.fromPkString().toPkString()} |")
    }

    @Test(dataProvider = "randomIds")
    fun `#toPkString(randomIds)`(expected: String, input: Int) {
        assertEquals(input.toPkString(), expected, "$input -> ${input.toPkString().fromPkString()} |")
    }

    @Test(dataProvider = "knownIds")
    fun `#toPkString(knownIds)`(expected: String, input: Int) {
        assertEquals(input.toPkString(), expected, "$input -> ${input.toPkString().fromPkString()} |")
    }

    @Test(dataProvider = "randomIds")
    fun `#isValidPkString(randomIds)`(input: String?, expected: Int) {
        assertTrue(input.isValidPkString(), "$expected |")
        assertNotNull(input, "`null` passed $expected |")
    }

    @Test(dataProvider = "knownIds")
    fun `#isValidPkString(knownIds)`(input: String?, expected: Int) {
        assertTrue(input.isValidPkString(), "$expected |")
        assertNotNull(input, "`null` passed $expected |")
    }

    @Test(dataProvider = "knownFirstFrees")
    fun `#firstFree(knownFirstFrees)`(list: Collection<String>, expected: String) {
        assertFalse(list.contains(expected), "Invalid test")
        assertEquals(list.firstFree(), expected)
    }

    @DataProvider
    fun knownFirstFrees() = arrayOf<Array<Any>>(
        arrayOf(listOf("aaaaa", "aaaab", "aaaac"), "aaaad"),
        arrayOf(listOf("aaaac", "aaaab", "aaaaa"), "aaaad"),
        arrayOf(listOf("zzzzz", "aaaaa", "ccccc"), "aaaab"),
        arrayOf(listOf("aaaab", "aaaac", "aaaad"), "aaaaa"),
    )

    @DataProvider
    fun knownIds() = arrayOf<Array<Any>>(
        arrayOf("aaaaa", 0),
        arrayOf("zzzzz", 11881375),
        arrayOf("aaaaz", 25),
        arrayOf("aaaab", 1),
        arrayOf("aaaba", 26),
        arrayOf("aabaa", 676),
        arrayOf("abaaa", 17576),
        arrayOf("baaaa", 456976),
    )

    @DataProvider
    fun randomIds(): Iterator<Array<Any>> = pkIdStream(100).mapToObj { arrayOf<Any>(it.toPkString(), it) }.iterator()
}