package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId
import java.time.Instant

class ProxiedMessageRecord {
    var _id: ObjectId = ObjectId()
    var creationDate = Instant.now()
    var oldMessageId: ULong = 0UL
    var newMessageId: ULong = 0UL
    var memberId: String = ""
    var systmId: String = ""
}