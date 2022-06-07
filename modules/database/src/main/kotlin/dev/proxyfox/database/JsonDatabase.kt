package dev.proxyfox.database

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import dev.proxyfox.database.DatabaseUtil.fromPkString
import dev.proxyfox.database.DatabaseUtil.toPkString
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.ServerSettingsRecord
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.misc.UserRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import java.io.File
import java.io.FileReader
import java.io.Reader
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
    private lateinit var users: MutableMap<ULong, UserRecord>
    private lateinit var servers: MutableMap<ULong, ServerSettingsRecord>

    fun setup() {
        val file = File("systems.json")
        val reader: Reader? = if (!file.exists())
            javaClass.getResourceAsStream("/assets/databases/defaultDatabase.json")?.reader()
        else FileReader(file)
        val db = JsonParser.parseReader(reader)
        val dbObject = db.asJsonObject
        if (!dbObject.has("schema")) {
            throw IllegalStateException("Database missing schema. Halt immediately.")
        }
        if (dbObject["schema"].asInt == 1) {
            systems = gson.fromJson(dbObject.getAsJsonObject("systems"), systemMapToken.type)
            users = gson.fromJson(dbObject.getAsJsonObject("users"), userMapToken.type)
            servers = gson.fromJson(dbObject.getAsJsonObject("servers"), serverMapToken.type)
        }
    }

    override suspend fun getUser(userId: String): UserRecord {
        return users[userId.toULong()] ?: run {
            val record = UserRecord()
            record.id = userId
            users[userId.toULong()] = record
            record
        }
    }

    override suspend fun getSystemByHost(userId: String): SystemRecord? {
        return systems[getUser(userId).system]?.view()
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        return systems[systemId]?.view()
    }

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? {
        return systems[getUser(userId).system]?.members?.values?.map(JsonMemberStruct::view)
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? {
        return systems[systemId]?.members?.values?.map(JsonMemberStruct::view)
    }

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? {
        return systems[getUser(userId).system]?.members?.get(memberId)?.view()
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        return systems[systemId]?.members?.get(memberId)?.view()
    }

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? {
        val system = systems[getUser(userId).system] ?: return null
        return system.switches.values.maxByOrNull { it.timestamp }?.memberIds?.mapNotNull { system.members[it]?.view() }
    }

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?>? {
        val system = systems[systemId] ?: return null
        return system.switches.values.maxByOrNull { it.timestamp }?.memberIds?.mapNotNull { system.members[it]?.view() }
    }

    override suspend fun getProxiesByHost(userId: String): List<MemberProxyTagRecord>? {
        return systems[getUser(userId).system]?.proxyTags
    }

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags
    }

    override suspend fun getProxiesByHostAndMember(userId: String, memberId: String): List<MemberProxyTagRecord>? {
        return systems[getUser(userId).system]?.proxyTags?.filter { it.memberId == memberId }
    }

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags?.filter { it.memberId == memberId }
    }

    override suspend fun getMemberFromMessage(userId: String, message: String): MemberRecord? {
        val system = systems[getUser(userId).system] ?: return null
        return system.members[getProxyTagFromMessage(userId, message)?.memberId]?.view()
    }

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? {
        val system = systems[getUser(userId).system] ?: return null
        return system.proxyTags.find { message.startsWith(it.prefix) && message.endsWith(it.suffix) }
    }

    override suspend fun getMemberServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return systems[getUser(userId).system]?.members?.get(memberId)?.serverSettings?.get(serverId.toULong())
    }

    override suspend fun getMemberServerSettingsById(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return systems[systemId]?.members?.get(memberId)?.serverSettings?.get(serverId.toULong())
    }

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? {
        return systems[getUser(userId).system]?.serverSettings?.get(serverId.toULong())
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

    override suspend fun allocateSystem(userId: String): SystemRecord {
        return getSystemByHost(userId) ?: run {
            val user = getUser(userId)
            val id = ((systems.keys.maxOfOrNull { it.fromPkString() } ?: 0) + 1).toPkString()
            val struct = JsonSystemStruct(id)
            struct.accounts.add(userId.toULong())
            user.system = id
            systems[id] = struct
            struct.view()
        }
    }

    override suspend fun removeSystem(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(systemId: String, memberId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateMember(member: MemberRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateSystem(system: SystemRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: UserRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getTrustLevel(userId: String, trustee: String): TrustLevel {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalSystems(): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalMembersByHost(userId: String): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalMembersById(systemId: String): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByHostAndName(userId: String, memberName: String): MemberRecord? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    class JsonSystemStruct(
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
        val proxyTags: List<MemberProxyTagRecord> = ArrayList(),
        val switches: MutableMap<String, SystemSwitchRecord> = HashMap()
    ) {

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
    }

    class JsonMemberStruct(
        val id: String,
        val systemId: String,
        var name: String = "",
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
    }

    companion object {
        private val gson = Gson()
        private val systemMapToken = object : TypeToken<MutableMap<String, JsonSystemStruct>>() {}
        private val userMapToken = object : TypeToken<MutableMap<ULong, UserRecord>>() {}
        private val serverMapToken = object : TypeToken<MutableMap<ULong, ServerSettingsRecord>>() {}
    }
}