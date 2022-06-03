package dev.proxyfox.database.records.system

import dev.proxyfox.database.records.misc.AutoProxyMode
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:07:21

/**
 * A mutable record representing a system.
 *
 * @author Ampflower
 **/
class SystemRecord {
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var users: ArrayList<String> = ArrayList()
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var avatarUrl: String? = null
    var timezone: String? = null
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null
    var autoType: AutoProxyMode = AutoProxyMode.OFF
}
