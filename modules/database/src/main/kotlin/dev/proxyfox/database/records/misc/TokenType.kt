/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TokenType.Serializer::class)
enum class TokenType(private val actualName: String) {
    SYSTEM_TRANSFER("system:transfer"),
    API_ACCESS("api:access"),
    API_EDIT("api:edit");

    override fun toString(): String {
        return actualName
    }

    fun canViewApi() = this == API_ACCESS || canEditApi()
    fun canEditApi() = this == API_EDIT

    companion object {
        fun of(name: String): TokenType? {
            for (type in TokenType.values()) {
                if (type.toString() == name) return type
            }
            return null
        }
    }
    class Serializer : KSerializer<TokenType> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("tokenType")
        override fun serialize(encoder: Encoder, value: TokenType) = encoder.encodeString(value.toString())
        override fun deserialize(decoder: Decoder): TokenType = TokenType.of(decoder.decodeString())!!
    }
}
