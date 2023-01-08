/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

// Created 2022-08-12T16:06:42

/**
 * Helper functions for [ScheduledExecutorService].
 *
 * @author Ampflower
 * @since 2.0.8
 **/

/**
 * Wrapper around [ScheduledExecutorService.scheduleAtFixedRate] to provide a Kotlin Coroutine equivalent.
 *
 * @param initialDelay The initial delay. Will be treated as milliseconds.
 * @param period The period in which future action runs will occur. Will be treated as milliseconds.
 * @param action The action to execute within a coroutine scope.
 * */
fun ScheduledExecutorService.fixedRateAction(initialDelay: Duration, period: Duration, action: suspend CoroutineScope.() -> Unit) {
    scheduleAtFixedRate({
        scope.launch(block = action)
    }, initialDelay.inWholeMilliseconds, period.inWholeMilliseconds, TimeUnit.MILLISECONDS)
}