/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Entity
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Instant
import org.testng.annotations.DataProvider
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

// Created 2022-22-09T01:36:24

/**
 * @author Ampflower
 * @since ${version}
 **/
object DatabaseTestUtil {
    private const val sysId1 = "aaaaa"
    private const val sysId2 = "aaaab"
    private const val sysId3 = "aaaac"
    private const val sysId4 = "exmpl"
    private const val sysId5 = "zzzzz"

    private val walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
    private val seed = System.getenv("TEST_SEED")?.toLongOrNull()
    private val rng = Random()

    const val offsetDateTimeEpochString = "1970-01-01T00:00:00Z"

    val instantEpoch = Instant.fromEpochSeconds(0L)
    val instantLastMicroOfEpochDay = Instant.fromEpochSeconds(TimeUnit.DAYS.toSeconds(1) - 1L)

    inline fun <reified T : Entity> entity(ret: ULong): T {
        return mockk {
            every { id } returns Snowflake(ret)
        }
    }

    @DataProvider
    @JvmStatic
    fun `16userIds`(): Iterator<Long> = seeded().longs(16).distinct().iterator()

    @DataProvider
    @JvmStatic
    fun `16pkIds`(): Iterator<String> = pkIdStream(16).mapToObj(Int::toPkString).iterator()

    @DataProvider
    @JvmStatic
    fun `16userToPkIdPairs`(): Iterator<Array<Any>> = seeded().run { GeneratingIterator(16) { arrayOf(nextLong(), nextPkId()) } }

    @DataProvider
    @JvmStatic
    fun `16pkIdPairs`(): Iterator<Array<Any>> = seeded().run { GeneratingIterator(16) { arrayOf(nextPkId(), nextPkId()) } }

    @JvmStatic
    fun pkIdStream(limit: Long): IntStream = Random(seed ?: genSeed()).ints(limit, 0, pkIdBound).distinct()

    @DataProvider(name = "seededRng")
    @JvmStatic
    fun seeded() = Random(seed ?: genSeed())

    private fun genSeed(): Long {
        val new = rng.nextLong() xor (System.nanoTime() shl 32) xor System.nanoTime()
        println("New seed $new used by ${walker.walk { it.skip(2).findFirst().orElse(null) }}")
        return new
    }

    fun Random.nextPkId() = nextInt(pkIdBound).toPkString()
}