/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.LocalDate

// Created 2022-02-10T23:23:07

/**
 * @author Ampflower
 * @since ${version}
 **/
class TimeUtilTest {

    @Test(dataProvider = "December 25th, 0001")
    fun `tryParseLocalDate - expect 00011225`(str: String) {
        assertEquals(tryParseLocalDate(str)?.first, dec25)
    }

    companion object {
        val dec25 = LocalDate.of(1, 12, 25)!!

        @JvmStatic
        @DataProvider
        fun `December 25th, 0001`() = arrayOf(
            // MMMM-DD-uuuu
            "December 25th, 0001",
            "December 25th",
            "December 25, 0001",
            "December 25",
            // MMM-DD-uuuu
            "Dec 25th, 0001",
            "Dec 25th",
            "Dec 25, 0001",
            "Dec 25",
            // DD-MMMM-uuuu
            "25th December 0001",
            "25th December",
            "25 December 0001",
            "25 December",
            // DD-MMM-uuuu
            "25th Dec 0001",
            "25th Dec",
            "25 Dec 0001",
            "25 Dec",
            // MM-DD-uuuu
            "12/25/0001",
            "12-25-0001",
            "12 25 0001",
            "12/25",
            "12-25",
            "12 25",
            // DD-MM-uuuu
            "25/12/0001",
            "25-12-0001",
            "25 12 0001",
            "25/12",
            "25-12",
            "25 12",
            // uuuu-MM-DD
            "0001-12-25",
            "0001/12/25",
            "0001 12 25",
        )
    }
}