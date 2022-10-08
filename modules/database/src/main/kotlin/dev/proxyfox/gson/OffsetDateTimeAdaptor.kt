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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object OffsetDateTimeAdaptor : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    override fun serialize(src: OffsetDateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null)
            JsonNull.INSTANCE
        else
            JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(src))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OffsetDateTime? {
        return json.asString.sanitise().run {
            if (isNullOrBlank()) {
                null
            } else {
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this, OffsetDateTime::from)
            }
        }
    }
}