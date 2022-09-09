package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId

class ChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var channelId: ULong = 0UL
    var proxyEnabled: Boolean = true

    fun writeTo(other: ChannelSettingsRecord) {
        other.proxyEnabled = proxyEnabled
    }
}
