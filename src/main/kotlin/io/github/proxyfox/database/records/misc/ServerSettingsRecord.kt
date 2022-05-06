package io.github.proxyfox.database.records.misc

// Created 2022-10-04T21:06:30

/**
 * @author Ampflower
 * @since ${version}
 **/
data class ServerSettingsRecord(
    val serverId: ULong,
    var proxyRole: ULong?,
    var disabledChannels: List<ULong>?
)