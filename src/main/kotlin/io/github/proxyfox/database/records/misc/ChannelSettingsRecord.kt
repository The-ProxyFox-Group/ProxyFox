package io.github.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake

data class ChannelSettingsRecord(
    val serverId: Snowflake,
    val channelId: Snowflake,
    var proxyEnabled: Boolean = true,
)
