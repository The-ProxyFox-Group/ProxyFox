/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.gson

import com.google.gson.*
import dev.proxyfox.database.sanitise
import java.lang.reflect.Type
import java.time.Instant
import java.time.format.DateTimeFormatter

object InstantAdaptor : JsonSerializer<Instant>, JsonDeserializer<Instant> {
    override fun serialize(src: Instant?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null)
            JsonNull.INSTANCE
        else
            JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Instant? {
        return json.asString.sanitise().run {
            if (isNullOrBlank()) {
                null
            } else {
                DateTimeFormatter.ISO_INSTANT.parse(this, Instant::from)
            }
        }
    }
}