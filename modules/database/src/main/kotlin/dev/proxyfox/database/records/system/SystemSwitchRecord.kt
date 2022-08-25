package dev.proxyfox.database.records.system

import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author KJP12
 **/
class SystemSwitchRecord {
    var _id: ObjectId = ObjectId()
    var systemId: String = ""
    var id: String = ""
    var memberIds: List<String> = ArrayList()
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}