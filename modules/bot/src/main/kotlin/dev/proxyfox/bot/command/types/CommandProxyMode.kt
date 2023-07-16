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
import dev.proxyfox.database.records.misc.AutoProxyMode
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

@JvmInline
value class CommandProxyMode(val value: AutoProxyMode)

@OptIn(InternalSerializationApi::class)
private class CommandProxyModeSerializer : CommandSerializer<CommandProxyMode> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CommandProxyMode")

    override fun decodeCommand(decoder: CommandDecoder): CommandProxyMode {
        decoder.cursor.checkout()
        val string = decoder.cursor.extractString(false).lowercase()
        decoder.cursor.inc()
        val mode = when (string) {
            "off", "disable", "o" -> AutoProxyMode.OFF
            "latch", "l", -> AutoProxyMode.LATCH
            "front", "f" -> AutoProxyMode.FRONT
            "fallback", "fb" -> AutoProxyMode.FALLBACK
            "member", "m" -> AutoProxyMode.MEMBER
            else -> {
                decoder.cursor.rollback()
                decoder.fails("Not AutoProxyMode")
            }
        }
        decoder.cursor.inc()
        return CommandProxyMode(mode)
    }

    override fun decodeRegular(decoder: Decoder): CommandProxyMode =
        CommandProxyMode(AutoProxyMode::class.serializer().deserialize(decoder))

    override fun serialize(encoder: Encoder, value: CommandProxyMode) =
        AutoProxyMode::class.serializer().serialize(encoder, value.value)
}
