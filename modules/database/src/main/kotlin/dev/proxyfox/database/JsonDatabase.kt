package dev.proxyfox.database

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import dev.kord.common.entity.Snowflake
import dev.proxyfox.common.logger
import dev.proxyfox.database.DatabaseUtil.fromPkString
import dev.proxyfox.database.DatabaseUtil.toPkString
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import org.bson.types.ObjectId
import java.io.File
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Created 2022-26-05T19:47:37

/**
 * JSON flat file database. Warning, all mutations *will* immediately take effect and are inherently unsafe.
 *
 * ## Expected File Structure
 * ```json5
 *   {
 *       "systems": {
 *           "sysid": {
 *               // here for redundancy purposes
 *               "id": "sysid",
 *               // list of user snowflakes of users that own the system (have it set as 'system' in their user object)
 *               "accounts": [],
 *               // name of the system
 *               "name": "name",
 *               // description that the user set
 *               "description": "description",
 *               // tag that shows when proxying to identify systems
 *               "tag": "tag",
 *               // default avatar for when a member doesn't have an avatar set
 *               "avatarUrl": "avatar url",
 *               // timestamp of creation
 *               "timestamp": "timestamp",
 *               // the member to autoproxy
 *               "auto", "memid",
 *               // the autoproxy type
 *               "autoType": "autoproxy type",
 *               "members": {
 *                   "memid": {
 *                       // here for redundancy
 *                       "id": "memid",
 *                       // name of the member, can be used to index
 *                       "name": "member name",
 *                       // display namme of the member, shown when proxying
 *                       "displayName": "display name",
 *                       // description that the member set
 *                       "description": "member description",
 *                       // birth date of the member
 *                       "birthday": "member birthday"
 *                       // age of the member
 *                       "age": "0",
 *                       // role of the member
 *                       "role": "role",
 *                       // pronouns of the member
 *                       "pronouns": "pronouns",
 *                       // member's avatar
 *                       "avatarUrl": "avatar url",
 *                       // whether to keep proxy tags
 *                       "keepProxy": false,
 *                       // the amount of messages sent by the member
 *                       "messageCount": 0,
 *                       // timestamp of when the member was created
 *                       "timestamp": "timestamp",
 *                       // per server settings
 *                       "serverSettings": {
 *                           "server snowflake": {
 *                               // display name
 *                               "displayName": "display name",
 *                               // avatar
 *                               "avatarUrl": "avatar URL",
 *                               // whether to proxy or not
 *                               "proxy": true
 *                           }
 *                       }
 *                   }
 *               },
 *               // per server
 *               "serverSettings": {
 *                   "server snowflake": {
 *                       // autoproxy type
 *                       "autoType": "autoproxy type",
 *                       // whether or not to proxy
 *                       "proxyEnabled": true
 *                   }
 *               },
 *               // array of proxy tags (each prefix/suffix pair will be unique)
 *               "proxyTags": [
 *                   {
 *                       "prefix": "prefix"
 *                       "suffix": "suffix",
 *                       "member": "memid"
 *                   }
 *               ],
 *               // array of switches
 *               "switches": {
 *                   "switch": {
 *                       // here for redundancy
 *                       "id": "switch"
 *                       // timestamp of the switch
 *                       "timestamp": "timestamp",
 *                       // array of fronting members
 *                       "members": [],
 *                   }
 *               }
 *           }
 *       },
 *       "users": {
 *           "user snowflake": {
 *               "system": "sysid",
 *               // appended to when systems grant trust
 *               "trusted": {
 *                   "sysid": "LEVEL"
 *               }
 *           }
 *       },
 *       "servers": {
 *           "server snowflake": {
 *               "channels": {
 *                   // false if disabled
 *                   "channel snowflake": true
 *               },
 *               // role to enable proxying
 *               "role": "role snowflake"
 *           }
 *       }
 *   }
 * ```
 *
 * @author Ampflower, Ram
 * @since ${version}
 **/
class JsonDatabase : Database() {
    private lateinit var systems: MutableMap<String, JsonSystemStruct>
    private lateinit var servers: MutableMap<ULong, ServerSettingsRecord>

    @Transient
    private val users = HashMap<ULong, JsonSystemStruct>()

    @Transient
    private val oldMessageLookup = HashMap<ULong, ProxiedMessageRecord>()
    @Transient
    private val newMessageLookup = HashMap<ULong, ProxiedMessageRecord>()

    override suspend fun setup(): JsonDatabase {
        val file = File("systems.json")
        if(file.exists()) {
            val db = file.reader().use(JsonParser::parseReader)
            if (db != null && db.isJsonObject) {
                val dbObject = db.asJsonObject
                if (!dbObject.has("schema")) {
                    throw IllegalStateException("JSON Database missing schema.")
                }
                if (dbObject["schema"].asInt == 1) {
                    systems = gson.fromJson(dbObject.getAsJsonObject("systems"), systemMapToken.type)
                    servers = gson.fromJson(dbObject.getAsJsonObject("servers"), serverMapToken.type)
                    for ((_, system) in systems) {
                        system.init()
                    }
                }

                for (system in systems.values) {
                    for (account in system.accounts) {
                        users[account] = system
                    }
                }

                return this
            }
        }
        systems = HashMap()
        servers = HashMap()

        return this
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "*")
    override suspend fun getUser(userId: String): UserRecord {
        throw UnsupportedOperationException()
    }

    override suspend fun getSystemByHost(userId: String): SystemRecord? {
        return users[userId.toULong()]?.view()
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        return systems[systemId]?.view()
    }

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? {
        return users[userId.toULong()]?.members?.values?.map(JsonMemberStruct::view)
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? {
        return systems[systemId]?.members?.values?.map(JsonMemberStruct::view)
    }

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? {
        return users[userId.toULong()]?.members?.get(memberId)?.view()
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        return systems[systemId]?.members?.get(memberId)?.view()
    }

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? {
        val system = users[userId.toULong()] ?: return null
        return system.switches.values.maxByOrNull { it.timestamp }?.memberIds?.mapNotNull { system.members[it]?.view() }
    }

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?>? {
        val system = systems[systemId] ?: return null
        return system.switches.values.maxByOrNull { it.timestamp }?.memberIds?.mapNotNull { system.members[it]?.view() }
    }

    override suspend fun getProxiesByHost(userId: String): List<MemberProxyTagRecord>? {
        return users[userId.toULong()]?.proxyTags
    }

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags
    }

    override suspend fun getProxiesByHostAndMember(userId: String, memberId: String): List<MemberProxyTagRecord>? {
        return users[userId.toULong()]?.proxyTags?.filter { it.memberId == memberId }
    }

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags?.filter { it.memberId == memberId }
    }

    override suspend fun getMemberFromMessage(userId: String, message: String): MemberRecord? {
        val system = users[userId.toULong()] ?: return null
        return system.members[getProxyTagFromMessage(userId, message)?.memberId]?.view()
    }

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? {
        val system = users[userId.toULong()] ?: return null
        return system.proxyTags.find { message.startsWith(it.prefix) && message.endsWith(it.suffix) }
    }

    override suspend fun getMemberServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return users[userId.toULong()]?.members?.get(memberId)?.serverSettings?.get(serverId.toULong())
    }

    override suspend fun getMemberServerSettingsById(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return systems[systemId]?.members?.get(memberId)?.serverSettings?.get(serverId.toULong())
    }

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? {
        return users[userId.toULong()]?.serverSettings?.get(serverId.toULong())
    }

    override suspend fun getServerSettingsById(serverId: String, systemId: String): SystemServerSettingsRecord {
        return systems[systemId]?.serverSettings?.get(serverId.toULong()) ?: SystemServerSettingsRecord().apply {
            this.serverId = serverId
            this.systemId = systemId
        }
    }

    override suspend fun getServerSettings(serverId: String): ServerSettingsRecord {
        return servers[serverId.toULong()] ?: ServerSettingsRecord().apply {
            this.serverId = serverId
        }
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        servers[serverSettings.serverId.toULong()] = serverSettings
    }

    override suspend fun getChannelSettings(channelId: String, systemId: String): SystemChannelSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: String): SystemRecord {
        return getSystemByHost(userId) ?: run {
            val id = ((systems.keys.maxOfOrNull { it.fromPkString() } ?: 0) + 1).toPkString()
            val struct = JsonSystemStruct(id)
            struct.accounts.add(userId.toULong())
            users[userId.toULong()] = struct
            systems[id] = struct
            struct.view()
        }
    }

    override suspend fun removeSystem(userId: String): Boolean {
        val id = userId.toULong()
        val system = users[id] ?: return false

        system.accounts.remove(id)
        users.remove(id, system)

        if (system.accounts.isEmpty()) {
            systems.remove(system.id)
        }

        return true
    }

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord {
        return getMemberByIdAndName(systemId, name) ?: run {
            val system = systems[systemId]!!
            val members = system.members
            val id = ((members.keys.maxOfOrNull { it.fromPkString() } ?: 0) + 1).toPkString()
            system.putMember(JsonMemberStruct(systemId, id, name))
        }
    }

    override suspend fun removeMember(systemId: String, memberId: String): Boolean {
        return systems[systemId]?.removeMember(memberId) ?: false
    }

    override suspend fun updateMember(member: MemberRecord) {
        systems[member.systemId]!!.members[member.id]!!.from(member)
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        systems[serverSettings.systemId]!!
            .members[serverSettings.memberId]!!
            .serverSettings[serverSettings.serverId.toULong()] = serverSettings
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems[system.id]!!.from(system)
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systems[serverSettings.systemId]!!
            .serverSettings[serverSettings.serverId.toULong()] = serverSettings
    }

    override suspend fun updateUser(user: UserRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun createMessage(
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        memberId: String,
        systemId: String
    ) {
        val message = ProxiedMessageRecord()
        message.oldMessageId = oldMessageId
        message.newMessageId = newMessageId
        message.memberId = memberId
        message.systmId = systemId
        oldMessageLookup[oldMessageId.value] = message
        newMessageLookup[newMessageId.value] = message
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if(prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        val proxies = systems[systemId]!!.proxyTags
        for(proxy in proxies) {
            if (proxy.prefix == prefix && proxy.suffix == suffix) {
                return if(proxy.memberId == memberId) {
                    // We would've created the proxy anyways.
                    proxy
                } else {
                    null
                }
            }
        }
        val proxy = MemberProxyTagRecord()
        proxy.systemId = systemId
        proxy.memberId = memberId
        proxy.prefix = prefix ?: ""
        proxy.suffix = suffix ?: ""
        proxies.add(proxy)
        return proxy
    }

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        systems[proxyTag.systemId]!!.proxyTags.remove(proxyTag)
    }

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getTrustLevel(userId: String, trustee: String): TrustLevel {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalSystems() = systems.size

    override suspend fun getTotalMembersByHost(userId: String): Int? {
        return users[userId.toULong()]?.members?.size
    }

    override suspend fun getTotalMembersById(systemId: String): Int? {
        return systems[systemId]?.members?.size
    }

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? {
        return systems[systemId]?.membersByName?.get(memberName)?.view()
    }

    override suspend fun getMemberByHostAndName(userId: String, memberName: String): MemberRecord? {
        return users[userId.toULong()]?.membersByName?.get(memberName)?.view()
    }

    override suspend fun export(other: Database) {
        for ((_, system) in systems) {
            other.import(system.view())
            for ((_, member) in system.members) {
                other.import(member.view())
                for ((_, memberSettings) in member.serverSettings) {
                    other.import(memberSettings)
                }
            }
        }
    }

    override suspend fun import(memberProxyTagRecord: MemberProxyTagRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(memberRecord: MemberRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(memberServerSettingsRecord: MemberServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(serverSettingsRecord: ServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(system: SystemRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(systemServerSettingsRecord: SystemServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun import(systemSwitchRecord: SystemSwitchRecord) {
        TODO("Not yet implemented")
    }

    override fun close() {
        val file = File("systems.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        val obj = JsonObject()
        obj.addProperty("schema", 1)
        obj.add("systems", gson.toJsonTree(systems))
        obj.add("users", gson.toJsonTree(users))
        obj.add("servers", gson.toJsonTree(servers))
        file.writer().use { gson.toJson(obj, it) }
    }

    data class JsonSystemStruct(
        val id: String,
        /** The user must have their snowflake bound to `system` to be included here. */
        val accounts: ArrayList<ULong> = ArrayList(),
        var name: String? = null,
        var description: String? = null,
        var tag: String? = null,
        var avatarUrl: String? = null,
        var timezone: String? = null,
        var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
        var auto: String? = null,
        var autoType: AutoProxyMode = AutoProxyMode.OFF,

        val members: MutableMap<String, JsonMemberStruct> = HashMap(),
        val serverSettings: MutableMap<ULong, SystemServerSettingsRecord> = HashMap(),
        val proxyTags: MutableList<MemberProxyTagRecord> = ArrayList(),
        val switches: MutableMap<String, SystemSwitchRecord> = HashMap()
    ) {
        /**
         * Cached lookup of name to member.
         * */
        @Transient
        lateinit var membersByName: HashMap<String, JsonMemberStruct>

        fun init() {
            val membersByName = HashMap<String, JsonMemberStruct>()
            for((_, member) in members) {
                val old = membersByName.put(member.name, member)
                if(old != null) logger.warn("Member {} collided with {} from {}", member, old, id)
            }
            this.membersByName = membersByName
        }

        fun view(): SystemRecord {
            val record = SystemRecord()
            record.id = id
            record.users.addAll(accounts.map(ULong::toString))
            record.name = name
            record.description = description
            record.tag = tag
            record.avatarUrl = avatarUrl
            record.timezone = timezone
            record.timestamp = timestamp
            record.autoProxy = auto
            record.autoType = autoType
            return record
        }

        fun from(record: SystemRecord) {
            accounts.clear()
            accounts.addAll(record.users.map(String::toULong))
            name = record.name
            description = record.description
            tag = record.tag
            avatarUrl = record.avatarUrl
            timezone = record.timezone
            timestamp = record.timestamp
            auto = record.autoProxy
            autoType = record.autoType
        }

        fun putMember(member: JsonMemberStruct): MemberRecord {
            members[member.id] = member
            membersByName[member.name] = member
            return member.view()
        }

        fun removeMember(member: String): Boolean {
            val struct = members.remove(member) ?: return false
            membersByName.remove(struct.name)

            proxyTags.removeIf { it.memberId == member }

            return true
        }
    }

    data class JsonMemberStruct(
        val id: String,
        val systemId: String,
        var name: String,
        var displayName: String? = null,
        var description: String? = null,
        var birthday: String? = null,
        var age: String? = null,
        var role: String? = null,
        var pronouns: String? = null,
        var color: Int = 0,
        var avatarUrl: String? = null,
        var keepProxy: Boolean = false,
        var messageCount: Long = 0L,
        var timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),

        val serverSettings: MutableMap<ULong, MemberServerSettingsRecord> = HashMap()
    ) {
        fun view(): MemberRecord {
            val record = MemberRecord()
            record.id = id
            record.systemId = systemId
            record.name = name
            record.displayName = displayName
            record.description = description
            record.birthday = birthday
            record.age = age
            record.role = role
            record.pronouns = pronouns
            record.color = color
            record.avatarUrl = avatarUrl
            record.keepProxy = keepProxy
            record.messageCount = messageCount
            record.timestamp = timestamp
            return record
        }

        fun from(record: MemberRecord) {
            name = record.name
            displayName = record.displayName
            description = record.description
            birthday = record.birthday
            age = record.age
            role = record.role
            pronouns = record.pronouns
            color = record.color
            avatarUrl = record.avatarUrl
            keepProxy = record.keepProxy
            messageCount = record.messageCount
            timestamp = record.timestamp
        }
    }

    companion object {
        private val gson = GsonBuilder()
            .registerTypeAdapter(OffsetDateTime::class.java, object : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
                override fun serialize(src: OffsetDateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                    return if (src == null)
                        JsonNull.INSTANCE
                    else
                        JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(src))
                }

                override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OffsetDateTime {
                    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.asString, OffsetDateTime::from)
                }
            }).registerTypeAdapter(ObjectId::class.java, object : JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {
                override fun serialize(src: ObjectId?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                    return JsonNull.INSTANCE
                }

                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ObjectId {
                    return ObjectId()
                }
            }).create()
        private val systemMapToken = object : TypeToken<HashMap<String, JsonSystemStruct>>() {}
        private val serverMapToken = object : TypeToken<HashMap<ULong, ServerSettingsRecord>>() {}
    }
}