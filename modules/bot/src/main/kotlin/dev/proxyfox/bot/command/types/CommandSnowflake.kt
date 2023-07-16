/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.types

import dev.kord.common.entity.Snowflake
import dev.proxyfox.command.CommandDecoder
import dev.proxyfox.command.types.CommandSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

@JvmInline
@Serializable(with = CommandSnowflakeSerializer::class)
value class CommandSnowflake(val snowflake: Snowflake)

@OptIn(InternalSerializationApi::class)
private class CommandSnowflakeSerializer : CommandSerializer<CommandSnowflake> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CommandSnowflake")

    override fun decodeCommand(decoder: CommandDecoder): CommandSnowflake {
        decoder.cursor.checkout()
        val num = decoder.cursor.extractString(false).toULongOrNull()
        decoder.cursor.inc()
        if (num == null) {
            decoder.cursor.rollback()
            decoder.fails("Not ULong")
        }
        decoder.cursor.commit()
        return CommandSnowflake(Snowflake(num))
    }

    override fun decodeRegular(decoder: Decoder): CommandSnowflake =
        CommandSnowflake(Snowflake::class.serializer().deserialize(decoder))

    override fun serialize(encoder: Encoder, value: CommandSnowflake) =
        Snowflake::class.serializer().serialize(encoder, value.snowflake)
}
