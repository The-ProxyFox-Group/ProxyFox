/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.gson

import com.google.gson.stream.JsonReader

// Created 2022-01-10T17:04:01

/**
 * @author Ampflower
 * @since ${version}
 **/
object NullValueProcessor : ValueProcessor<Any?> {
    override fun ifArray(reader: JsonReader) = reader.skip()
    override fun ifBoolean(reader: JsonReader) = reader.skip()
    override fun ifString(reader: JsonReader) = reader.skip()
    override fun ifNumber(reader: JsonReader) = reader.skip()

    private fun JsonReader.skip(): Any? {
        skipValue()
        return null
    }
}