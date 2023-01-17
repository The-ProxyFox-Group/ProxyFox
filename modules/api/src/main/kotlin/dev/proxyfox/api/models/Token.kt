/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.misc.TokenRecord
import dev.proxyfox.database.records.misc.TokenType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String,
    @SerialName("system_id")
    val systemId: String,
    val type: TokenType
) {
    companion object {
        fun fromRecord(record: TokenRecord) = Token(record.token, record.systemId, record.type)
    }
}
