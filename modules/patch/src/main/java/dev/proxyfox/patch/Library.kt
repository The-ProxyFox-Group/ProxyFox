/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch

import net.fabricmc.api.EnvType

// Created 2023-03-01T01:43:20
/**
 * @author Ampflower
 * @since 2.0.8
 */
interface Library {
    fun envType(): EnvType?
    fun paths(): List<String?>
    fun path(): String? = paths()[0]
}