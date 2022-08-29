package dev.proxyfox.database.records.misc

import org.bson.types.ObjectId

// Created 2022-10-04T21:06:30

/**
 * @author Ampflower
 * @since ${version}
 **/
class ServerSettingsRecord {
    var _id: ObjectId = ObjectId()
    var serverId: ULong = 0UL
    var proxyRole: ULong = 0UL
    var disabledChannels: List<ULong>? = null

    fun writeTo(other: ServerSettingsRecord) {
        other.proxyRole = proxyRole
        other.disabledChannels = disabledChannels
    }
}