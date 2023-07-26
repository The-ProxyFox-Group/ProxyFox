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
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonType

// Created 2023-08-01T21:50:05

/**
 * [Instant] serializer with the resolution of milliseconds, used for
 * storing non-critical timestamps such as when a system was created.
 *
 * Non-critical, as it doesn't risk mangling PluralKit data if reimported
 * from our exports.
 *
 * @author Ampflower
 * @since 2.1
 **/
object InstantLongMillisecondSerializer : KSerializer<Instant> {
    private const val microsecondReference = 1_000_000L

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

    /**
     * Bypass the decoder API in the case of BSON.
     * */
    override fun deserialize(decoder: Decoder): Instant {
        val long = if (decoder is BsonFlexibleDecoder && decoder.reader.currentBsonType == BsonType.DATE_TIME) {
            // This evidently should've been millisecond, but we've been
            // storing the microsecond precision, so this must be done.
            decoder.reader.readDateTime()
        } else {
            decoder.decodeLong()
        }
        return Instant.fromEpochSeconds(long / 1000L, long.mod(1000L) * microsecondReference)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        if (encoder is BsonEncoder) {
            encoder.encodeDateTime(value.epochMilliseconds)
        } else {
            encoder.encodeLong(value.epochMilliseconds)
        }
    }

    val Instant.epochMilliseconds
        get() = epochSeconds * 1000L + (nanosecondsOfSecond / microsecondReference)
}