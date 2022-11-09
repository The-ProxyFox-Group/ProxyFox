/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import kotlinx.serialization.Serializable

@Serializable
data class System(
    val id: String,
    val name: String?,
    val description: String?,
    val tag: String?,
    val pronouns: String?,
    val color: String,
    val avatarUrl: String?,
    val timezone: String?,
    val timestamp: String
)