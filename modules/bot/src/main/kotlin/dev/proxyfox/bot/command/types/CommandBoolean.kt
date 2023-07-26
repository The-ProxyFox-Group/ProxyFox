/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.types

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
@Serializable(with = CommandBooleanSerializer::class)
value class CommandBoolean(val value: Boolean)

@OptIn(InternalSerializationApi::class)
private class CommandBooleanSerializer : CommandSerializer<CommandBoolean> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CommandBoolean")

    override fun decodeCommand(decoder: CommandDecoder): CommandBoolean {
        decoder.cursor.checkout()
        val bool = decoder.cursor.extractString(false).lowercase()
        if (arrayOf("true", "false", "enable", "disable", "on", "off", "1", "0").contains(bool)) {
            decoder.cursor.rollback()
            decoder.fails("Not boolean")
        }
        decoder.cursor.commit()
        val value = arrayOf("true", "enable", "on", "1").contains(bool)
        return CommandBoolean(value)
    }

    override fun decodeRegular(decoder: Decoder): CommandBoolean =
        CommandBoolean(Boolean::class.serializer().deserialize(decoder))

    override fun serialize(encoder: Encoder, value: CommandBoolean) =
        Boolean::class.serializer().serialize(encoder, value.value)
}
