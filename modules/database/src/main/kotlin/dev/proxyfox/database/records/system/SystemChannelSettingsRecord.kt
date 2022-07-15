package dev.proxyfox.database.records.system

import org.bson.types.ObjectId

class SystemChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: String = ""
    var channelId: String = ""
    var systemId: String = ""
    var proxyEnabled: Boolean = true
}
