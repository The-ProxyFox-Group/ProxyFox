package dev.proxyfox.database.records.system

import org.bson.types.ObjectId

class SystemChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var channelId: ULong = 0UL
    var systemId: String = ""
    var proxyEnabled: Boolean = true

    fun writeTo(other: SystemChannelSettingsRecord) {
        other.proxyEnabled = proxyEnabled
    }
}
