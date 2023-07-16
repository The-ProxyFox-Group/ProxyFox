/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.types

import dev.kord.core.entity.Attachment
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.command.CommandDecoder
import dev.proxyfox.command.types.CommandSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

@JvmInline
@Serializable(with = CommandAttachmentSerializer::class)
value class CommandAttachment(val attachment: Attachment)

@OptIn(InternalSerializationApi::class)
private class CommandAttachmentSerializer : CommandSerializer<CommandAttachment> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CommandAttachment")

    override fun decodeCommand(decoder: CommandDecoder): CommandAttachment {
        val context = decoder.context as? DiscordContext<Any> ?: decoder.fails("Not discord context")
        val attachment = context.getAttachment() ?: decoder.fails("No attachment")
        return CommandAttachment(attachment)
    }

    override fun decodeRegular(decoder: Decoder): CommandAttachment =
        CommandAttachment(Attachment::class.serializer().deserialize(decoder))

    override fun serialize(encoder: Encoder, value: CommandAttachment) =
        Attachment::class.serializer().serialize(encoder, value.attachment)
}
