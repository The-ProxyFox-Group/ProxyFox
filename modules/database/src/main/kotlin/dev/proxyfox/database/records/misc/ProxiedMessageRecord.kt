package dev.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake
import org.bson.types.ObjectId
import java.time.Instant

class ProxiedMessageRecord {
    var _id: ObjectId = ObjectId()
    var creationDate = Instant.now()
    var oldMessageId: Snowflake = Snowflake(0)
    var newMessageId: Snowflake = Snowflake(0)
    var memberId: String = ""
    var systmId: String = ""
}