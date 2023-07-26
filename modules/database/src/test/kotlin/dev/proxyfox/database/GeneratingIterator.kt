/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

// Created 2022-23-09T23:18:31

/**
 * @author Ampflower
 * @since ${version}
 **/
class GeneratingIterator<T>(private val limit: Int, private val generator: () -> T) : Iterator<T> {
    var i = 0

    override fun hasNext(): Boolean = i < limit

    override fun next(): T {
        if (i >= limit) throw NoSuchElementException()
        i++
        return generator()
    }
}