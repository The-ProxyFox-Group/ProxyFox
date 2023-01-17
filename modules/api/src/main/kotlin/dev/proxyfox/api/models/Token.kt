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
