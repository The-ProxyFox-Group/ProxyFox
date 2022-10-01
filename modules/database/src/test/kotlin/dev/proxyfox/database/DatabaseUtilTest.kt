/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.google.gson.reflect.TypeToken
import dev.proxyfox.database.DatabaseTestUtil.pkIdStream
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

// Created 2022-22-09T01:35:21

/**
 * @author KJP12
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

    @Test
    fun `RecordAdapter(GenericStore) - expect list`() {
        val record = readJson<GenericStore<List<Any?>>>("""{"value":["a","b","c"]}""")
        assertEquals(record.value, listOf("a", "b", "c"))
    }

    @Test
    fun `RecordAdapter(GenericStore) - expect array`() {
        val record = readJson<GenericStore<Array<Any?>>>("""{"value":["a","b","c"]}""")
        assertEquals(record.value, arrayOf("a", "b", "c"))
    }

    @Test
    fun `RecordAdapter(GenericStore) - expect map A`() {
        val record = readJson<GenericStore<Map<*, *>>>("""{"value":{"integer":123,"string":"Hi!","integerMap":{"a":1,"b":2,"c":3}}}""")
        assertEquals(
            record.value, mapOf(
                "integer" to 123.0,
                "string" to "Hi!",
                "integerMap" to mapOf("a" to 1.0, "b" to 2.0, "c" to 3.0)
            )
        )
    }

    @Test
    fun `RecordAdapter(GenericStore) - expect map B`() {
        val record = readJson<GenericStore<Map<*, *>>>("""{"value":{"integer":123,"string":"Hi!","integerMap":{"1":"a","2":"b","3":"c"}}}""")
        assertEquals(
            record.value, mapOf(
                "integer" to 123.0,
                "string" to "Hi!",
                "integerMap" to mapOf("1" to "a", "2" to "b", "3" to "c")
            )
        )
    }

    @Test
    fun `RecordAdapter(GenericStore) - expect ComplexStore`() {
        val record = readJson<GenericStore<ComplexStore>>("""{"value":{"integer":123,"string":"Hi!","integerMap":{"1":"a","2":"b","3":"c"}}}""")
        assertEquals(
            record.value, ComplexStore(
                integer = 123,
                string = "Hi!",
                integerMap = mapOf(1 to "a", 2 to "b", 3 to "c")
            )
        )
    }

    @Test
    fun `RecordAdapter(ComplexStore) - expect working`() {
        val record = readJson<ComplexStore>("""{"integer":123,"string":"Hi!","integerMap":{"1":"a","2":"b","3":"c"}}""")
        assertEquals(
            record, ComplexStore(
                integer = 123,
                string = "Hi!",
                integerMap = mapOf(1 to "a", 2 to "b", 3 to "c")
            )
        )
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

    private inline fun <reified T> readJson(str: String): T {
        return gson.fromJson(str, object : TypeToken<T>() {}.type)
    }

    @JvmRecord
    data class GenericStore<T>(val value: T)

    @JvmRecord
    data class ComplexStore(
        val integer: Int,
        val string: String,
        val integerMap: Map<Int, String>
    )
}