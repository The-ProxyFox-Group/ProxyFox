/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.proxyfox.common.ellipsis
import dev.proxyfox.common.fromColor
import dev.proxyfox.common.logger
import dev.proxyfox.common.toColor
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

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
 * @author KJP12, Ram
 * @since ${version}
 **/
class JsonDatabase(val file: File = File("systems.json")) : Database() {
    private lateinit var systems: MutableMap<String, JsonSystemStruct>
    private lateinit var servers: MutableMap<ULong, ServerSettingsRecord>
    private lateinit var channels: MutableMap<ULong, ChannelSettingsRecord>
    private lateinit var messages: MutableSet<ProxiedMessageRecord>

    @Transient
    private lateinit var users: MutableMap<ULong, JsonSystemStruct>

    @Transient
    private lateinit var messageMap: MutableMap<ULong, ProxiedMessageRecord>

    @Volatile
    private var dropped: Boolean = false

    override suspend fun setup(): JsonDatabase {
        users = ConcurrentHashMap()
        messageMap = ConcurrentHashMap()
        if (file.exists()) {
            val db = file.reader().use(JsonParser::parseReader)
            if (db != null && db.isJsonObject) {
                read(db.asJsonObject)

                return this
            }
        }
        init()

        return this
    }

    @TestOnly
    fun read(dbObject: JsonObject) {
        if (!dbObject.has("schema")) {
            throw IllegalStateException("JSON Database missing schema.")
        }
        if (dbObject["schema"].asInt == 1) {
            systems = gson.fromJson(dbObject.getAsJsonObject("systems"), systemMapToken.type) ?: ConcurrentHashMap()
            servers = gson.fromJson(dbObject.getAsJsonObject("servers"), serverMapToken.type) ?: ConcurrentHashMap()
            channels = gson.fromJson(dbObject.getAsJsonObject("channels"), channelMapToken.type) ?: ConcurrentHashMap()
            messages = gson.fromJson(dbObject.getAsJsonArray("messages"), messageSetToken.type) ?: HashSet()
            for ((_, system) in systems) {
                system.init()
            }
            for (message in messages) {
                messageMap[message.oldMessageId] = message
                messageMap[message.newMessageId] = message
            }
        }

        for (system in systems.values) {
            for (account in system.accounts) {
                users[account] = system
            }
        }
        OffsetDateTime.now().offset.totalSeconds
    }

    @TestOnly
    fun init() {
        systems = ConcurrentHashMap()
        servers = ConcurrentHashMap()
        channels = ConcurrentHashMap()
        messages = HashSet()
    }

    override suspend fun ping(): Duration {
        return Duration.ZERO
    }

    override suspend fun getDatabaseName() = "JSON"

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchUser(userId: ULong): UserRecord? {
        return users[userId]?.let { UserRecord().apply { id = userId; systemId = it.id } }
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? {
        return systems[systemId]?.view()
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord>? {
        return systems[systemId]?.members?.values?.map(JsonMemberStruct::view)
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? {
        return systems[systemId]?.members?.get(memberId)?.view()
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchFrontingMembersFromSystem(systemId: String): List<MemberRecord>? {
        val system = systems[systemId] ?: return null
        return fetchLatestSwitch(systemId)?.memberIds?.mapNotNull { system.members[it]?.view() }
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags?.map { it.view(systemId) }
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        return systems[systemId]?.proxyTags?.filter { it.member == memberId }?.map { it.view(systemId) }
    }

    override suspend fun fetchMemberFromMessage(userId: ULong, message: String): MemberRecord? {
        val system = users[userId] ?: return null
        return system.members[fetchProxyTagFromMessage(userId, message)?.memberId]?.view()
    }

    override suspend fun fetchProxyTagFromMessage(userId: ULong, message: String): MemberProxyTagRecord? {
        val system = users[userId] ?: return null
        return system.proxyTags.find {
            if (it.prefix != null && it.suffix != null)
                return@find message.startsWith(it.prefix!!) && message.endsWith(it.suffix!!)
            if (it.prefix != null)
                return@find message.startsWith(it.prefix!!)
            if (it.suffix != null)
                return@find message.endsWith(it.suffix!!)
            return@find false
        }?.view(system.id)
    }

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return systems[systemId]?.members?.get(memberId)?.serverSettings?.run {
            get(serverId) ?: MemberServerSettingsRecord().apply {
                this.serverId = serverId
                this.systemId = systemId
                this.memberId = memberId
            }
        }
    }

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        return systems[systemId]?.serverSettings?.let {
            it[serverId] ?: SystemServerSettingsRecord().apply {
                this.serverId = serverId
                this.systemId = systemId
            }
        } ?: fail("No such system $systemId")
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord {
        return servers[serverId] ?: ServerSettingsRecord().apply {
            this.serverId = serverId
        }
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        servers[serverSettings.serverId] = serverSettings
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        return systems[systemId]?.channelSettings?.let {
            it[channelId] ?: SystemChannelSettingsRecord().apply {
                this.channelId = channelId
                this.systemId = systemId
            }
        } ?: fail("No such system $systemId")
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        return channels[channelId] ?: ChannelSettingsRecord().apply {
            this.serverId = serverId
            this.channelId = channelId
        }
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        channels[channel.channelId] = channel
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        return fetchSystemFromUser(userId) ?: run {
            val sysId = if (id == null || systems.containsKey(id)) systems.keys.firstFree() else id
            val struct = JsonSystemStruct(sysId)
            struct.accounts.add(userId)
            initSystem(struct).view()
        }
    }

    private fun initSystem(struct: JsonSystemStruct): JsonSystemStruct {
        for (user in struct.accounts) users[user] = struct
        systems[struct.id] = struct
        struct.init()
        return struct
    }

    override suspend fun containsSystem(systemId: String): Boolean {
        return systems.containsKey(systemId)
    }

    override suspend fun dropSystem(userId: ULong): Boolean {
        val system = users[userId] ?: return false

        system.accounts.remove(userId)
        users.remove(userId, system)

        if (system.accounts.isEmpty()) {
            systems.remove(system.id)
        }

        return true
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun getOrCreateMember(systemId: String, name: String, id: String?): MemberRecord? {
        return fetchMemberFromSystemAndName(systemId, name) ?: run {
            val system = systems[systemId] ?: return null
            val memId = if (id == null || system.members.containsKey(id)) system.members.keys.firstFree() else id
            system.putMember(JsonMemberStruct(id = memId, systemId = systemId, name = name)).view()
        }
    }

    override suspend fun containsMember(systemId: String, memberId: String): Boolean {
        return systems[systemId]?.members?.containsKey(memberId) ?: false
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        return systems[systemId]?.removeMember(memberId) ?: false
    }

    override suspend fun updateMember(member: MemberRecord) {
        systems[member.systemId]!!.run {
            members[member.id]?.from(member, this) ?: run { putMember(JsonMemberStruct(member)) }
        }
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        systems[serverSettings.systemId]!!
            .members[serverSettings.memberId]!!
            .serverSettings[serverSettings.serverId] = serverSettings
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems[system.id]?.from(system) ?: initSystem(JsonSystemStruct(system))
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systems[serverSettings.systemId]!!
            .serverSettings[serverSettings.serverId] = serverSettings
    }

    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        systems[channelSettings.systemId]!!
            .channelSettings[channelSettings.channelId] = channelSettings
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Non-native method")
    override suspend fun updateUser(user: UserRecord) {
        if (user.systemId == null) {
            dropSystem(user.id)
        } else {
            val system = systems[user.systemId] ?: throw IllegalArgumentException("No such system ${user.systemId}")
            users[user.id] = system
            system.accounts.add(user.id)
        }
    }

    override suspend fun createMessage(
        userId: Snowflake,
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        channelBehavior: ChannelBehavior,
        memberId: String,
        systemId: String,
        memberName: String
    ) {
        val message = ProxiedMessageRecord()
        message.memberName = memberName
        message.oldMessageId = oldMessageId.value
        message.newMessageId = newMessageId.value
        val channel = channelBehavior.fetchChannel()
        when (channel) {
            is ThreadChannel -> {
                message.channelId = channel.parentId.value
                message.threadId = channel.id.value
            }
            else -> message.channelId = channel.id.value
        }
        message.guildId = channel.data.guildId.value?.value ?: 0UL
        message.memberId = memberId
        message.systemId = systemId
        messages.add(message)
        messageMap[oldMessageId.value] = message
        messageMap[newMessageId.value] = message
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        messageMap[message.oldMessageId]?.let { old ->
            messageMap.remove(old.newMessageId)
            messages.remove(message)
        }
        messageMap[message.oldMessageId] = message
        messages.add(message)
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? {
        return messageMap[messageId.value]
    }

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? {
        // TODO: Better caching logic
        return messages.firstOrNull { it.channelId == channelId.value && it.systemId == systemId }
    }

    override suspend fun createProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if (prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        val proxies = systems[systemId]!!.proxyTags
        proxies.firstOrNull { it.prefix == prefix && it.suffix == suffix }?.let {
            return if (it.member == memberId) it.view(systemId) else null
        }
        val proxy = MemberProxyTagRecord()
        proxy.systemId = systemId
        proxy.memberId = memberId
        proxy.prefix = prefix ?: ""
        proxy.suffix = suffix ?: ""
        proxies.add(JsonProxyStruct.from(proxy))
        return proxy
    }

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord? {
        val system = systems[systemId] ?: return null
        val switch = SystemSwitchRecord()
        val id = ((system.switches.keys.maxOfOrNull { it.fromPkString() } ?: 0) + 1).toPkString()
        switch.id = id
        switch.systemId = systemId
        switch.memberIds = memberId
        timestamp?.let { switch.timestamp = it }
        system.switches[id] = switch
        return switch
    }

    override suspend fun dropSwitch(switch: SystemSwitchRecord) {
        systems[switch.systemId]?.run { switches.remove(switch.id) }
    }

    override suspend fun updateSwitch(switch: SystemSwitchRecord) {
        systems[switch.systemId]?.run { switches[switch.id] = switch }
    }

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord>? {
        return systems[systemId]?.switches?.values?.toList()
    }

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
        systems[proxyTag.systemId]!!.proxyTags.remove(JsonProxyStruct.from(proxyTag))
    }

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
        val system = systems[systemId] ?: return false
        system.trust[trustee] = level
        return true
    }

    override suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel {
        return systems[systemId]?.trust?.get(trustee) ?: TrustLevel.NONE
    }

    override suspend fun fetchTotalSystems() = systems.size

    override suspend fun fetchTotalMembersFromSystem(systemId: String): Int? {
        return systems[systemId]?.members?.size
    }

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String): MemberRecord? {
        return systems[systemId]?.membersByName?.get(memberName)?.view()
    }

    override suspend fun export(other: Database) {
        val memberLookup = HashMap<String, String>()
        logger.info("Migrating {} systems$ellipsis", systems.size)
        for ((sid, system) in systems) {
            memberLookup.clear()
            logger.info("Migrating {}: {}", sid, system.name)
            val newSystem = other.getOrCreateSystem(system.accounts[0], sid)
            system.writeTo(newSystem)
            other.updateSystem(newSystem)

            val nsid = newSystem.id

            logger.info("Migrating {} members$ellipsis", system.members.size)
            for ((mid, member) in system.members) {
                logger.info("Migrating {}: {}", mid, member.name)
                val newMember = other.getOrCreateMember(sid, member.name, mid)
                if (newMember == null) {
                    logger.warn("Unable to import {}: {} didn't return a member for {}/{} ({}/???)?", member.name, other, sid, mid, nsid)
                } else {
                    memberLookup[mid] = newMember.id
                    member.writeTo(newMember)
                    other.updateMember(newMember)
                }
            }

            for ((id, serverSettings) in system.serverSettings) {
                val newSettings = other.getOrCreateServerSettingsFromSystem(id, nsid)
                serverSettings.writeTo(newSettings, memberLookup[serverSettings.autoProxy])
                other.updateSystemServerSettings(newSettings)
                logger.info("Written server settings for {}", id)
            }

            for ((id, channelSettings) in system.channelSettings) {
                val newSettings = other.getOrCreateChannelSettingsFromSystem(id, nsid)
                channelSettings.writeTo(newSettings)
                updateSystemChannelSettings(newSettings)
                logger.info("Written channel settings for {}", id)
            }

            for (proxyTag in system.proxyTags) {
                val member = memberLookup[proxyTag.member]
                if (member == null) {
                    logger.warn("Couldn't write proxy tag {}text{} for {}/{} ({}/???)", proxyTag.prefix, proxyTag.suffix, sid, proxyTag.member, nsid)
                } else {
                    other.createProxyTag(nsid, member, proxyTag.prefix, proxyTag.suffix)
                    logger.info("Written proxy tag {}text{} for {}", proxyTag.prefix, proxyTag.suffix, member)
                }
            }

            for ((id, switch) in system.switches) {
                val newSwitch = other.createSwitch(nsid, switch.memberIds.mapNotNull(memberLookup::get), switch.timestamp)
                if (newSwitch == null) {
                    logger.warn("Couldn't write switch {}/{} to {}", sid, id, nsid)
                } else {
                    logger.info("Written switch {}/{} to {}/{}", sid, id, nsid, newSwitch.id)
                }
            }
        }
        logger.info("Migrating server settings$ellipsis")
        for ((sid, server) in servers) {
            val newSettings = other.getOrCreateServerSettings(sid)
            server.writeTo(newSettings)
            other.updateServerSettings(newSettings)
            logger.info("Written server settings for {}", sid)
        }
    }

    @Deprecated("Not for regular use.", level = DeprecationLevel.ERROR)
    override suspend fun drop() {
        dropped = true

        systems.clear()
        servers.clear()
        channels.clear()
        messages.clear()
        users.clear()
        messageMap.clear()

        if (file.exists()) {
            file.delete()
        }
    }

    override fun close() {
        if (dropped) return
        val obj = JsonObject()
        obj.addProperty("schema", 1)
        obj.add("systems", gson.toJsonTree(systems))
        obj.add("servers", gson.toJsonTree(servers))
        obj.add("channels", gson.toJsonTree(channels))
        obj.add("messages", gson.toJsonTree(messages))
        file.writer().use { gson.toJson(obj, it) }
    }

    data class JsonProxyStruct(
        var member: String,
        var prefix: String? = null,
        var suffix: String? = null
    ) {
        fun view(systemId: String): MemberProxyTagRecord {
            val proxy = MemberProxyTagRecord()
            proxy.memberId = member
            proxy.systemId = systemId
            proxy.prefix = prefix
            proxy.suffix = suffix
            return proxy
        }

        companion object {
            fun from(proxy: MemberProxyTagRecord): JsonProxyStruct {
                return JsonProxyStruct(proxy.memberId, proxy.prefix, proxy.suffix)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other !is JsonProxyStruct) return false
            return member == other.member && suffix == other.suffix && prefix == other.prefix
        }
    }

    data class JsonSystemStruct(
        val id: String,
        /** The user must have their snowflake bound to `system` to be included here. */
        val accounts: ArrayList<ULong> = ArrayList(),
        var name: String? = null,
        var description: String? = null,
        var tag: String? = null,
        var pronouns: String? = null,
        var color: Int = -1,
        var avatarUrl: String? = null,
        var timezone: String? = null,
        var timestamp: OffsetDateTime? = OffsetDateTime.now(ZoneOffset.UTC),
        var auto: String? = null,
        var autoType: AutoProxyMode? = AutoProxyMode.OFF,

        val members: MutableMap<String, JsonMemberStruct> = HashMap(),
        val serverSettings: MutableMap<ULong, SystemServerSettingsRecord> = HashMap(),
        val channelSettings: MutableMap<ULong, SystemChannelSettingsRecord> = HashMap(),
        val proxyTags: MutableList<JsonProxyStruct> = ArrayList(),
        val switches: MutableMap<String, SystemSwitchRecord> = HashMap(),
        val trust: HashMap<ULong, TrustLevel> = HashMap()
    ) {
        constructor(record: SystemRecord) : this(record.id) {
            from(record)
        }

        /**
         * Cached lookup of name to member.
         * */
        @Transient
        lateinit var membersByName: HashMap<String, JsonMemberStruct>

        fun init() {
            val membersByName = HashMap<String, JsonMemberStruct>()
            for ((_, member) in members) {
                val old = membersByName.put(member.name, member)
                member.systemId = id
                if (old != null) logger.warn("Member {} collided with {} from {}", member, old, id)
            }
            this.membersByName = membersByName
        }

        fun view(): SystemRecord {
            val record = SystemRecord()
            record.id = id
            writeTo(record)
            return record
        }

        fun writeTo(record: SystemRecord) {
            record.users.addAll(accounts)
            record.name = name
            record.description = description
            record.tag = tag
            record.pronouns = pronouns
            record.color = color
            record.avatarUrl = avatarUrl
            record.timezone = timezone
            timestamp?.let { record.timestamp = it }
            record.autoProxy = auto
            autoType?.let {
                record.autoType = it
            }
            record.trust = trust
        }

        fun from(record: SystemRecord) {
            accounts.clear()
            accounts.addAll(record.users)
            name = record.name
            description = record.description
            tag = record.tag
            pronouns = record.pronouns
            color = record.color
            avatarUrl = record.avatarUrl
            timezone = record.timezone
            timestamp = record.timestamp
            auto = record.autoProxy
            autoType = record.autoType
        }

        fun putMember(member: JsonMemberStruct): JsonMemberStruct {
            assert(member.systemId == id) { "systemId != id" }
            members[member.id] = member
            membersByName[member.name] = member
            return member
        }

        fun removeMember(member: String): Boolean {
            val struct = members.remove(member) ?: return false
            membersByName.remove(struct.name)

            proxyTags.removeIf { it.member == member }

            return true
        }
    }

    data class JsonMemberStruct(
        val id: String,
        var systemId: String,
        var name: String,
        var displayName: String? = null,
        var description: String? = null,
        var birthday: LocalDate? = null,
        var age: String? = null,
        var role: String? = null,
        var pronouns: String? = null,
        var color: String? = null,
        var avatarUrl: String? = null,
        var keepProxy: Boolean = false,
        var messageCount: ULong = 0UL,
        var timestamp: OffsetDateTime? = OffsetDateTime.now(ZoneOffset.UTC),

        val serverSettings: MutableMap<ULong, MemberServerSettingsRecord> = HashMap()
    ) {

        constructor(record: MemberRecord) : this(record.id, record.systemId, record.name) {
            from(record)
        }

        fun view(): MemberRecord {
            val record = MemberRecord()
            record.id = id
            record.systemId = systemId
            writeTo(record)
            return record
        }

        fun writeTo(record: MemberRecord) {
            record.name = name
            record.displayName = displayName
            record.description = description
            record.birthday = birthday
            record.age = age
            record.role = role
            record.pronouns = pronouns
            record.color = color.toColor()
            record.avatarUrl = avatarUrl
            record.keepProxy = keepProxy
            record.messageCount = messageCount
            timestamp?.let { record.timestamp = it }
        }

        fun JsonSystemStruct.from(record: MemberRecord) = from(record, this)

        fun from(record: MemberRecord, system: JsonSystemStruct? = null) {
            if (name != record.name && system != null) {
                system.membersByName.remove(name)
                system.membersByName[record.name] = this
                name = record.name
            }
            displayName = record.displayName
            description = record.description
            birthday = record.birthday
            age = record.age
            role = record.role
            pronouns = record.pronouns
            color = record.color.fromColor()
            avatarUrl = record.avatarUrl
            keepProxy = record.keepProxy
            messageCount = record.messageCount
            timestamp = record.timestamp
        }
    }

    companion object {
        private val systemMapToken = object : TypeToken<ConcurrentHashMap<String, JsonSystemStruct>>() {}
        private val serverMapToken = object : TypeToken<ConcurrentHashMap<ULong, ServerSettingsRecord>>() {}
        private val channelMapToken = object : TypeToken<ConcurrentHashMap<ULong, ChannelSettingsRecord>>() {}
        private val messageSetToken = object : TypeToken<HashSet<ProxiedMessageRecord>>() {}
    }
}