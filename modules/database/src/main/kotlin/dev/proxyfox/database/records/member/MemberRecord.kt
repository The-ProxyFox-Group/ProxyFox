package dev.proxyfox.database.records.member

import dev.proxyfox.database.database
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

// Created 2022-09-04T14:12:07

/**
 * A mutable record representing a system's member.
 *
 * @author Ampflower
 **/
class MemberRecord {
    var _id: ObjectId = ObjectId()
    var id: String = ""
    var systemId: String = ""
    var name: String = ""
    var displayName: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: Int = -1
    var avatarUrl: String? = null

    /** Whether the proxy tag remains in the message */
    var keepProxy: Boolean = false
    var messageCount: ULong = 0UL
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var birthday: String? = null
    var age: String? = null
    var role: String? = null

    suspend fun serverName(serverId: ULong) =
        if (serverId == 0UL) null else database.getMemberServerSettingsById(serverId, systemId, id)?.nickname
}
