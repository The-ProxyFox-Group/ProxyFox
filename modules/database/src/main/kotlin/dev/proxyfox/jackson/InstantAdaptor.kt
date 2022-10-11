/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import de.undercouch.bson4jackson.BsonGenerator
import dev.proxyfox.jackson.InstantAdaptor.intHandle
import dev.proxyfox.jackson.InstantAdaptor.longHandle
import java.lang.invoke.MethodHandles
import java.nio.ByteOrder
import java.time.Instant
import java.util.*

// Created 2022-09-10T19:50:39

private object InstantAdaptor {
    // BIG_ENDIAN is selected as it is orderable via that.
    @JvmField
    val longHandle = MethodHandles.byteArrayViewVarHandle(LongArray::class.java, ByteOrder.BIG_ENDIAN)!!

    @JvmField
    val intHandle = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.BIG_ENDIAN)!!
}

/**
 * @author Ampflower
 * @since ${version}
 **/
object InstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant? {
        return when (val obj = p.embeddedObject) {
            is ByteArray -> Instant.ofEpochSecond(longHandle.get(obj, 0) as Long, (intHandle.get(obj, 8) as Int).toLong())
            is Date -> obj.toInstant()
            is String -> Instant.parse(obj)
            else -> when (p.currentToken) {
                JsonToken.VALUE_STRING -> Instant.parse(p.valueAsString)
                JsonToken.VALUE_NUMBER_INT -> Instant.ofEpochMilli(p.valueAsLong)
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
            val arr = ByteArray(12)
            longHandle.set(arr, 0, value.epochSecond)
            intHandle.set(arr, 8, value.nano)
            gen.writeBinary(arr)
            return
        }
        gen.writeString(value.toString())
    }
}