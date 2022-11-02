/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import de.undercouch.bson4jackson.BsonGenerator
import java.time.Instant
import java.util.*

// Created 2022-09-10T19:50:39

/**
 * @author KJP12
 * @since ${version}
 **/
object InstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant? {
        return when (val obj = p.embeddedObject) {
            is Long -> Instant.ofEpochSecond(obj / 1000000, obj.mod(1000000L) * 1000L)
            is Date -> obj.toInstant()
            is String -> Instant.parse(obj)
            else -> when (p.currentToken) {
                JsonToken.VALUE_STRING -> Instant.parse(p.valueAsString)
                JsonToken.VALUE_NUMBER_INT -> p.valueAsLong.let { Instant.ofEpochSecond(it / 100000, (it / 1000L).mod(1000000L)) }
                else -> {
                    p.skipChildren()
                    null
                }
            }
        }
    }
}

object InstantSerializer : JsonSerializer<Instant>() {
    override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
            return
        }
        if (gen is BsonGenerator) {
            gen.writeNumber((value.epochSecond * 1000000) + (value.nano / 1000))
            return
        }
        gen.writeString(value.toString())
    }
}