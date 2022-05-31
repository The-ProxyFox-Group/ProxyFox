package io.github.proxyfox.database.records.misc

import org.bson.types.ObjectId

class UserRecord {
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var system: String? = null
    var trust: HashMap<String, TrustLevel> = HashMap()
}