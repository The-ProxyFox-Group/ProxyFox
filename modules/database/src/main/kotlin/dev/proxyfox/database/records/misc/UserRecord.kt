package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId

class UserRecord {
    var _id: ObjectId = ObjectId()
    var id: ULong = 0UL
    var system: String? = null
}