package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId

// Created 2022-10-04T21:06:30

/**
 * @author KJP12
 * @since ${version}
 **/
class ServerSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var proxyRole: ULong = 0UL

    fun writeTo(other: ServerSettingsRecord) {
        other.proxyRole = proxyRole
    }
}