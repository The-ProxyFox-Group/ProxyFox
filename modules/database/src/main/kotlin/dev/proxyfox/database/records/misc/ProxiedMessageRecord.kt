package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ProxiedMessageRecord {
    var _id: ObjectId = ObjectId()
    var creationDate = OffsetDateTime.now(ZoneOffset.UTC)
    var oldMessageId: ULong = 0UL
    var newMessageId: ULong = 0UL
    var channelId: ULong = 0UL
    var memberId: String = ""
    var systemId: String = ""
}