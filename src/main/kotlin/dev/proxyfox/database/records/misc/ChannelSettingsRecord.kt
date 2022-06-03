package dev.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake
import org.bson.types.ObjectId

class ChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: Snowflake = Snowflake(0)
    var channelId: Snowflake = Snowflake(0)
    var proxyEnabled: Boolean = true
}
