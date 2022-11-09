package dev.proxyfox.api.models

import dev.proxyfox.database.records.system.SystemSwitchRecord

data class Switch(
    val id: String,
    val members: List<String>,
    val timestamp: String
) {
    companion object {
        fun fromRecord(record: SystemSwitchRecord) = Switch(
            id = record.id,
            members = record.memberIds,
            timestamp = record.timestamp.toString()
        )
    }
}
