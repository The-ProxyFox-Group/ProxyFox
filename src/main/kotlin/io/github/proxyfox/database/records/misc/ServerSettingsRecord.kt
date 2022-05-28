package io.github.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake

// Created 2022-10-04T21:06:30

/**
 * @author Ampflower
 * @since ${version}
 **/
data class ServerSettingsRecord(
    val serverId: Snowflake,
    var proxyRole: Snowflake?,
    var disabledChannels: List<ULong>?
)