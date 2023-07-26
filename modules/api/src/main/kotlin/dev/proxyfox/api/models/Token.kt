/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.PkId
import dev.proxyfox.database.records.misc.TokenRecord
import dev.proxyfox.database.records.misc.TokenType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a token
 *
 * Accessed via the `/tokens` route
 *
 * Requires a token to access.
 *
 * @param token the token
 * @param systemId the ID for the system it's attached to
 * @param type the type of token it is
 * */
@Serializable
data class Token(
    val token: String,
    @SerialName("system_id")
    val systemId: PkId,
    val type: TokenType
) {
    companion object {
        fun fromRecord(record: TokenRecord) = Token(record.token, record.systemId, record.type)
    }
}
