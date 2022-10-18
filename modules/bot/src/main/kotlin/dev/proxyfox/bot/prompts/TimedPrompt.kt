/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.prompts

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.proxyfox.bot.scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Created 2022-17-10T05:20:36

/**
 * @author KJP12
 * @since ${version}
 **/
abstract class TimedPrompt(
    protected val runner: Snowflake,
    protected val reference: Message,
    timeout: Duration = oneMinute,
) {
    protected lateinit var jobs: Collection<Job>
    private var lastUpdate = Clock.System.now()

    private val timerJob = scope.launch {
        val delay = maxOf(timeout, oneMinute)
        var now = Clock.System.now()
        var comparison = lastUpdate + delay

        // Busy wait for cancelling the prompt.
        while (comparison > now) {
            delay(maxOf(comparison - now, oneMinute))
            now = Clock.System.now()
            comparison = lastUpdate + delay
        }

        close()
    }

    abstract suspend fun close()

    protected fun bump() {
        lastUpdate = Clock.System.now()
    }

    protected open suspend fun closeInternal() {
        jobs.forEach(Job::cancel)
        timerJob.cancel()
    }

    companion object {
        val oneMinute = 1.minutes
    }
}