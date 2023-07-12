/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Coffee(
    val id: String,
    var state: BrewState,
)

@Serializable
enum class BrewState {
    STARTING,
    BREWING,
    READY;

    fun advance(): BrewState = when (this) {
        STARTING -> BREWING
        BREWING -> READY
        READY -> READY
    }
}