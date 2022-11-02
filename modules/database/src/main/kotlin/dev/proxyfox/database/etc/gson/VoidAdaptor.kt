/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object VoidAdaptor : TypeAdapter<Void>() {
    override fun write(out: JsonWriter, value: Void?) {
        out.nullValue()
    }

    override fun read(input: JsonReader): Void? {
        input.skipValue()
        return null
    }
}