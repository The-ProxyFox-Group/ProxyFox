/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * @author Ampflower
 * @since ${version}
 **/
class UriUtilTest {
    @Test(dataProvider = "passHttp")
    fun `UriUtil - expect pass http`(uri: String) {
        assertNotNull(uri.httpUriOrNull())
    }

    @Test(dataProvider = "failHttp")
    fun `UriUtil - expect fail http`(uri: String) {
        assertNull(uri.httpUriOrNull())
    }

    companion object {

        @DataProvider
        @JvmStatic
        fun passHttp() = UriUtilTest::class.java.getResourceAsStream("uris/pass.txt")!!.bufferedReader().lines().iterator()

        @DataProvider
        @JvmStatic
        fun failHttp() = UriUtilTest::class.java.getResourceAsStream("uris/fail.txt")!!.bufferedReader().lines().iterator()
    }
}