/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.gson

import com.google.gson.*
import java.lang.reflect.Type

object ULongAdaptor : JsonSerializer<ULong>, JsonDeserializer<ULong> {
    override fun serialize(src: ULong?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src.toLong())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ULong {
        return json.asLong.toULong()
    }
}