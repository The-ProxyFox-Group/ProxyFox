package io.github.proxyfox.database.records.system

import dev.kord.common.entity.Snowflake

data class SystemChannelSettingsRecord(
    val serverId: Snowflake,
    val channelId: Snowflake,
    val systemId: Snowflake,
    var proxyEnabled: Boolean = true
)
