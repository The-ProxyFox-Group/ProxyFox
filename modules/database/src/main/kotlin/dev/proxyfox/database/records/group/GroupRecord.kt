package dev.proxyfox.database.records.group

import dev.proxyfox.database.PkId
import dev.proxyfox.database.records.MongoRecord
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GroupRecord() : MongoRecord {
    constructor(id: PkId, systemId: PkId, name: String) : this() {
        this.id = id
        this.systemId = systemId
        this.name = name
    }

    override var _id: ObjectId = ObjectId()

    var id: PkId = ""
    var systemId: PkId = ""
    var members: ArrayList<PkId> = arrayListOf()
    var name: String = ""
    var description: String? = null
    var color: Int = -1
    var avatarUrl: String? = null
    var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
    var tag: String? = null
    var tagMode: TagMode = TagMode.HIDDEN
}