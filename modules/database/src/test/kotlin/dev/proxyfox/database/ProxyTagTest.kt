/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.proxyfox.database.records.member.MemberProxyTagRecord
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Factory
import org.testng.annotations.Test

// Created 2022-14-10T12:25:12

/**
 * @author Ampflower
 * @since ${version}
 **/
class ProxyTagTest @Factory(dataProvider = "constructorParameters") constructor(
    val proxy: MemberProxyTagRecord,
    val pair: Pair<String, String>
) {
    @Test(dataProvider = "randomStrings")
    fun `#test - expect pass`(str: String) {
        assertTrue(proxy.test(pair text str))
    }

    @Test(dataProvider = "stringsForSuffix")
    fun `#test - suffix only`(str: String) {
        assertFalse(proxy.test(pair suffix str))
    }

    @Test(dataProvider = "stringsForPrefix")
    fun `#test - prefix only`(str: String) {
        assertFalse(proxy.test(pair prefix str))
    }

    @Test
    fun `#isEqual - pass - self reference`() {
        assertTrue(proxy.isEqual(proxy))
    }

    @Test
    fun `#isEqual - pass - pair reference`() {
        assertTrue(proxy.isEqual(pair.first, pair.second))
    }

    @Test
    fun `#isEqual - pass - new reference`() {
        assertTrue(proxy.isEqual(MemberProxyTagRecord("zzzzz", "zzzzz", pair.first, pair.second)))
    }

    @Test
    fun `#isEqual - fail - null`() {
        assertFalse(proxy.isEqual(null, null))
        assertFalse(proxy.isEqual(MemberProxyTagRecord("aaaaa", "aaaaa", null, null)))
        assertFalse(proxy.isEqual(MemberProxyTagRecord("zzzzz", "zzzzz", null, null)))
    }

    @Test
    fun `#isEqual - fail - empty`() {
        assertFalse(proxy.isEqual("", ""))
        assertFalse(proxy.isEqual(MemberProxyTagRecord("aaaaa", "aaaaa", "", "")))
        assertFalse(proxy.isEqual(MemberProxyTagRecord("zzzzz", "zzzzz", "", "")))
    }

    @Test(dataProvider = "randomStrings")
    fun `#trim - pass`(str: String) {
        assertEquals(proxy.trim(pair text str), str)
    }

    @DataProvider
    fun stringsForSuffix() = if (pair.first == "") emptyArray() else randomStrings()

    @DataProvider
    fun stringsForPrefix() = if (pair.second == "") emptyArray() else randomStrings()

    override fun toString() = "Proxy ${pair.text("text")}"

    companion object {
        @JvmStatic
        @DataProvider
        fun constructorParameters() = arrayOf(
            arrayOf(MemberProxyTagRecord("aaaaa", "aaaaa", "a", "a"), "a" to "a"),
            arrayOf(MemberProxyTagRecord("aaaaa", "aaaaa", "z", "z"), "z" to "z"),
            arrayOf(MemberProxyTagRecord("aaaaa", "aaaaa", "a:", ""), "a:" to ""),
            arrayOf(MemberProxyTagRecord("aaaaa", "aaaaa", "", "-z"), "" to "-z"),
        )

        @JvmStatic
        @DataProvider
        fun randomStrings() = arrayOf(
            "text",
            "pineapple",
            "potato",
            "azaleas",
            "salad",
            ""
        )

        infix fun Pair<String, String>.text(str: String) = "$first$str$second"

        // NUL is used to provide a guaranteed invalid.
        infix fun Pair<String, String>.suffix(str: String) = "\u0000$str$second"

        // NUL is used to provide a guaranteed invalid.
        infix fun Pair<String, String>.prefix(str: String) = "$first$str\u0000"
    }
}