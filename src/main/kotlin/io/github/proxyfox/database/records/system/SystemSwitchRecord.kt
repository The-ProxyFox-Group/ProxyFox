package io.github.proxyfox.database.records.system

import org.bson.types.ObjectId

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author Ampflower
 **/
class SystemSwitchRecord {
    var _id: ObjectId = ObjectId()
    var systemId: String = ""
    var id: String = ""
    var memberIds: List<String> = ArrayList()
    var timestamp: String = ""
}