/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox

// Created 2022-12-10T19:53:43

/**
 * @author Ampflower
 * @since ${version}
 **/
interface BlackHole {
    fun swallow(any: Any?)

    fun <T, R> swallowAndExecute(t: T, action: (T) -> R): R {
        return action(t)
    }

    companion object {
        val instance: BlackHole = object : BlackHole {
            override fun swallow(any: Any?) {
            }
        }
    }
}