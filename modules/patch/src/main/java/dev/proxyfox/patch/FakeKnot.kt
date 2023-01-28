/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:JvmName("FakeKnot")

package dev.proxyfox.patch

import org.quiltmc.loader.impl.launch.knot.KnotServer

// Created 2023-05-01T20:11:34

/**
 * @author KJP12
 * @since 2.0.8
 **/

fun main(args: Array<String>) = KnotServer.main(args)