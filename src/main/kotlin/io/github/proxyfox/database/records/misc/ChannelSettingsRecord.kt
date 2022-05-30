package io.github.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake

class ChannelSettingsRecord {
    var serverId: Snowflake = Snowflake(0)
    var channelId: Snowflake = Snowflake(0)
    var proxyEnabled: Boolean = true
}
