package io.github.proxyfox.database.records.system

import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author Ampflower
 **/
data class SystemSwitchRecord(
    val systemId: String,
    val id: String,
    var memberIds: List<String>,
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
)