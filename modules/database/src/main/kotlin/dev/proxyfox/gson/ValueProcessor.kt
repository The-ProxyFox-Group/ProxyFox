/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.gson

import com.google.gson.stream.JsonReader
import dev.proxyfox.database.unsupported

// Created 2022-01-10T03:41:13

/**
 * @author KJP12
 * @since ${version}
 **/
interface ValueProcessor<T> {
    fun ifArray(reader: JsonReader): T = unsupported()
    fun ifBoolean(reader: JsonReader): T = unsupported()
    fun ifString(reader: JsonReader): T = unsupported()
    fun ifNumber(reader: JsonReader): T = unsupported()
}