package io.github.proxyfox.database.records.system

import org.bson.types.ObjectId

class SystemChannelSettingsRecord {
    var _id: ObjectId = ObjectId()
    val serverId: String = ""
    val channelId: String = ""
    val systemId: String = ""
    var proxyEnabled: Boolean = true
}
