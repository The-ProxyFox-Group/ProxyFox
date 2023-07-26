/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.ktx.serializaton

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonType
import java.util.concurrent.TimeUnit

// Created 2023-08-01T21:50:05

/**
 * [LocalDate] serializer with the resolution of milliseconds, used for
 * retrieving and storing timestamps from the Mongo database.
 *
 * @author Ampflower
 * @since 2.1
 **/
object LocalDateLongMillisecondSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.LONG)

    /**
     * Bypass the decoder API in the case of BSON.
     * */
    override fun deserialize(decoder: Decoder): LocalDate {
        val long = if (decoder is BsonFlexibleDecoder && decoder.reader.currentBsonType == BsonType.DATE_TIME) {
            // This evidently should've been millisecond, but we've been
            // storing the microsecond precision, so this must be done.
            decoder.reader.readDateTime()
        } else {
            decoder.decodeLong()
        }
        return LocalDate.fromEpochDays((long / TimeUnit.DAYS.toMillis(1)).toInt())
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(value.epochMilliseconds)
        } else {
            encoder.encodeLong(value.epochMilliseconds)
        }
    }

    val LocalDate.epochMilliseconds
        get() = toEpochDays() * TimeUnit.DAYS.toMillis(1L)
}