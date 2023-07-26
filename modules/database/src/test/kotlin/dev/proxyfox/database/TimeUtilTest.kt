/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import kotlinx.datetime.LocalDate
import org.testng.Assert.assertEquals
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

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

    @Test(dataProvider = "January 1st, +65535")
    fun `tryParseLocalDate - expect 655350101`(str: String) {
        assertEquals(tryParseLocalDate(str)?.first, jan01)
    }

    @Test(dataProvider = "January 1st, -65536")
    fun `tryParseLocalDate - expect -655360101`(str: String) {
        assertEquals(tryParseLocalDate(str)?.first, jan01neg)
    }

    companion object {
        val dec25 = LocalDate(1, 12, 25)
        val jan01 = LocalDate(65535, 1, 1)
        val jan01neg = LocalDate(-65536, 1, 1)

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

        @JvmStatic
        @DataProvider
        fun `January 1st, +65535`() = arrayOf(
            // MMMM-DD-uuuu
            "January 1st, 65535",
            "January 1, 65535",
            // MMM-DD-uuuu
            "Jan 1st, 65535",
            "Jan 1, 65535",
            // DD-MMMM-uuuu
            "1st January 65535",
            "1 January 65535",
            // DD-MMM-uuuu
            "1st Jan 65535",
            "1 Jan 65535",
            // MM-DD-uuuu
            "1/1/65535",
            "1-1-65535",
            "1 1 65535",
            // DD-MM-uuuu
            "1/1/65535",
            "1-1-65535",
            "1 1 65535",
            // uuuu-MM-DD
            "65535-1-1",
            "65535/1/1",
            "65535 1 1",

            // == This time, with the plus prefix

            // MMMM-DD-uuuu
            "January 1st, +65535",
            "January 1, +65535",
            // MMM-DD-uuuu
            "Jan 1st, +65535",
            "Jan 1, +65535",
            // DD-MMMM-uuuu
            "1st January +65535",
            "1 January +65535",
            // DD-MMM-uuuu
            "1st Jan +65535",
            "1 Jan +65535",
            // MM-DD-uuuu
            "1/1/+65535",
            "1-1-+65535",
            "1 1 +65535",
            // DD-MM-uuuu
            "1/1/+65535",
            "1-1-+65535",
            "1 1 +65535",
            // uuuu-MM-DD
            "+65535-1-1",
            "+65535/1/1",
            "+65535 1 1",
        )

        @JvmStatic
        @DataProvider
        fun `January 1st, -65536`() = arrayOf(
            // MMMM-DD-uuuu
            "January 1st, -65536",
            "January 1, -65536",
            // MMM-DD-uuuu
            "Jan 1st, -65536",
            "Jan 1, -65536",
            // DD-MMMM-uuuu
            "1st January -65536",
            "1 January -65536",
            // DD-MMM-uuuu
            "1st Jan -65536",
            "1 Jan -65536",
            // MM-DD-uuuu
            "1/1/-65536",
            "1-1--65536",
            "1 1 -65536",
            // DD-MM-uuuu
            "1/1/-65536",
            "1-1--65536",
            "1 1 -65536",
            // uuuu-MM-DD
            "-65536-1-1",
            "-65536/1/1",
            "-65536 1 1",
        )
    }
}