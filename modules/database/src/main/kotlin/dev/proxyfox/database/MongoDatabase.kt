/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.util.KMongoUtil
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.Duration


// Created 2022-26-05T22:43:40

typealias Mongo = com.mongodb.reactivestreams.client.MongoDatabase
typealias KCollection<T> = MongoCollection<T>

/**
 * @author KJP12, Emma
 * @since ${version}
 **/
class MongoDatabase(private val dbName: String = "ProxyFox") : Database() {
    private lateinit var kmongo: MongoClient
    private lateinit var db: Mongo

    private lateinit var users: KCollection<UserRecord>

    private lateinit var messages: KCollection<ProxiedMessageRecord>

    private lateinit var servers: KCollection<ServerSettingsRecord>
    private lateinit var channels: KCollection<ChannelSettingsRecord>

    private lateinit var systems: KCollection<SystemRecord>
    private lateinit var systemSwitches: KCollection<SystemSwitchRecord>

    private lateinit var systemServers: KCollection<SystemServerSettingsRecord>
    private lateinit var systemChannels: KCollection<SystemChannelSettingsRecord>

    private lateinit var members: KCollection<MemberRecord>
    private lateinit var memberProxies: KCollection<MemberProxyTagRecord>

    private lateinit var memberServers: KCollection<MemberServerSettingsRecord>

    override suspend fun setup(): MongoDatabase {
        val connectionString = System.getenv("PROXYFOX_MONGO")
        kmongo =
            if (connectionString.isNullOrEmpty())
                KMongo.createClient()
            else
                KMongo.createClient(connectionString)
        db = kmongo.getDatabase(dbName)

        users = db.getOrCreateCollection()

        messages = db.getOrCreateCollection()
        messages.createIndex(
            Indexes.ascending("creationDate"),
            IndexOptions().expireAfter(1L, TimeUnit.DAYS)
        ).awaitFirst()

        servers = db.getOrCreateCollection()
        channels = db.getOrCreateCollection()

        systems = db.getOrCreateCollection()
        systemSwitches = db.getOrCreateCollection()

        systemServers = db.getOrCreateCollection()
        systemChannels = db.getOrCreateCollection()

        members = db.getOrCreateCollection()
        memberProxies = db.getOrCreateCollection()

        memberServers = db.getOrCreateCollection()

        return this
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun ping(): Duration {
        return measureTime {
            db.runCommand<Any>("{ping: 1}").awaitFirst()
        }
    }

    override suspend fun getDatabaseName() = "MongoDB"

    override suspend fun fetchUser(userId: ULong): UserRecord? {
        return users.findOne("{id:$userId}")
    }

    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? = systems.findOne("{id:'$systemId'}")

    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord> =
        members.findAll("{systemId:'$systemId'}")

    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? {
        return members.findOne("{id:'$memberId', systemId:'$systemId'}")
    }

    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId'}")

    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord =
        memberServers.findOne("{serverId:$serverId,systemId:'$systemId',memberId:'$memberId'}") ?: MemberServerSettingsRecord().apply {
            this.serverId = serverId
            this.systemId = systemId
            this.memberId = memberId
        }

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        var serverSettings = systemServers.findOne("{serverId:$serverId,systemId:'$systemId'}")
        if (serverSettings == null) {
            serverSettings = SystemServerSettingsRecord()
            serverSettings.serverId = serverId
            serverSettings.systemId = systemId
            systemServers.insertOne(serverSettings).awaitFirst()
        }
        return serverSettings
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord {
        var serverSettings = servers.findOne("{serverId:$serverId}")
        if (serverSettings == null) {
            serverSettings = ServerSettingsRecord()
            serverSettings.serverId = serverId
            servers.insertOne(serverSettings).awaitFirst()
        }
        return serverSettings
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        servers.deleteOneById(serverSettings._id).awaitFirst()
        servers.insertOne(serverSettings).awaitFirst()
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        return systemChannels.findOne("{channelId:$channelId,systemId:'$systemId'}") ?: SystemChannelSettingsRecord().apply {
            this.channelId = channelId
            this.systemId = systemId
            systemChannels.insertOne(this).awaitFirst()
        }
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        return channels.findOne("{channelId:$channelId}")
            ?: ChannelSettingsRecord().apply {
                this.serverId = serverId
                this.channelId = channelId
            }
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        channels.deleteOneById(channel._id).awaitFirst()
        channels.insertOne(channel).awaitFirst()
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        return fetchSystemFromUser(userId) ?: run {
            val user = getOrCreateUser(userId)
            val system = SystemRecord()
            system.id = if (isSystemIdReserved(id)) systems.find().toList().map(SystemRecord::id).firstFree() else id
            system.users.add(userId)
            user.systemId = system.id
            updateUser(user)
            this.systems.insertOne(system).awaitFirst()
            system
        }
    }

    override suspend fun dropSystem(userId: ULong): Boolean {
        val system = fetchSystemFromUser(userId) ?: return false
        val filter = KMongoUtil.toBson("{systemId:'${system.id}'}")
        systemServers.deleteMany(filter).awaitFirst()
        systemChannels.deleteMany(filter).awaitFirst()
        systemSwitches.deleteMany(filter).awaitFirst()
        memberProxies.deleteMany(filter).awaitFirst()
        memberServers.deleteMany(filter).awaitFirst()
        members.deleteMany(filter).awaitFirst()
        systems.deleteOneById(system._id).awaitFirst()
        users.deleteMany(filter).awaitFirst()
        return true
    }

    override suspend fun getOrCreateMember(systemId: String, name: String, id: String?): MemberRecord? {
        fetchSystemFromId(systemId) ?: return null
        val member = MemberRecord()
        member.id = if (isMemberIdReserved(systemId, id)) fetchMembersFromSystem(systemId).map(MemberRecord::id).firstFree() else id
        member.name = name
        member.systemId = systemId
        this.members.insertOne(member).awaitFirst()
        return member
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        val member = fetchMemberFromSystem(systemId, memberId) ?: return false
        val filter = KMongoUtil.toBson("{systemId:'$systemId',memberId:'$memberId'}")
        memberProxies.deleteMany(filter).awaitFirst()
        memberServers.deleteMany(filter).awaitFirst()
        members.deleteOneById(member._id).awaitFirst()
        return true
    }

    override suspend fun updateMember(member: MemberRecord) {
        members.deleteOneById(member._id).awaitFirst()
        members.insertOne(member).awaitFirst()
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        memberServers.deleteOneById(serverSettings._id).awaitFirst()
        memberServers.insertOne(serverSettings).awaitFirst()
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems.deleteOneById(system._id).awaitFirst()
        systems.insertOne(system).awaitFirst()
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systemServers.deleteOneById(serverSettings._id).awaitFirst()
        systemServers.insertOne(serverSettings).awaitFirst()
    }

    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        systemChannels.deleteOneById(channelSettings._id).awaitFirst()
        systemChannels.insertOne(channelSettings).awaitFirst()
    }

    override suspend fun updateUser(user: UserRecord) {
        users.deleteOneById(user._id).awaitFirst()
        users.insertOne(user).awaitFirst()
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
        message.userId = userId.value
        message.oldMessageId = oldMessageId.value
        message.newMessageId = newMessageId.value
        val channel = channelBehavior.fetchChannel()
        message.guildId = channel.data.guildId.value?.value ?: 0UL
        when (channel) {
            is ThreadChannel -> {
                message.channelId = channel.parentId.value
                message.threadId = channel.id.value
            }
            else -> message.channelId = channel.id.value
        }
        message.memberId = memberId
        message.systemId = systemId
        messages.insertOne(message).awaitFirst()
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        messages.deleteOneById(message._id).awaitFirst()
        messages.insertOne(message).awaitFirst()
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? =
        messages.findOne("{\$or:[{'newMessageId':$messageId},{'oldMessageId':$messageId}]}")

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? =
        messages.find().filter("{'systemId':'$systemId','channelId':$channelId}").sort("{'creationDate':-1}").limit(1).awaitFirstOrNull()

    override suspend fun createProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if (prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        fetchProxiesFromSystem(systemId).firstOrNull { prefix == it.prefix && suffix == it.suffix }?.let {
            return if (it.memberId == memberId) it else null
        }
        val proxy = MemberProxyTagRecord()
        proxy.prefix = prefix ?: ""
        proxy.suffix = suffix ?: ""
        proxy.memberId = memberId
        proxy.systemId = systemId
        memberProxies.insertOne(proxy).awaitFirst()
        return proxy
    }

    override suspend fun fetchProxyTags(systemId: String, memberId: String) =
        memberProxies.findAll("{systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: OffsetDateTime?): SystemSwitchRecord? {
        fetchSystemFromId(systemId) ?: return null
        val switches = fetchSwitchesFromSystem(systemId)
        val switch = SystemSwitchRecord()
        val id = ((switches.maxOfOrNull { it.id.fromPkString() } ?: 0) + 1).toPkString()
        switch.id = id
        switch.systemId = systemId
        switch.memberIds = memberId
        timestamp?.let { switch.timestamp = it }
        systemSwitches.insertOne(switch).awaitFirst()
        return switch
    }

    override suspend fun dropSwitch(switch: SystemSwitchRecord) {
        systemSwitches.deleteOneById(switch._id).awaitFirst()
    }

    override suspend fun updateSwitch(switch: SystemSwitchRecord) {
        systemSwitches.deleteOneById(switch._id).awaitFirst()
        systemSwitches.insertOne(switch).awaitFirst()
    }

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord> =
        systemSwitches.findAll("{systemId:'$systemId'}")

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies.deleteOne("{systemId:'${proxyTag.systemId}',memberId:'${proxyTag.memberId}',prefix:'${proxyTag.prefix}',suffix:'${proxyTag.suffix}'}")
            .awaitFirst()
    }

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
        val system = fetchSystemFromId(systemId) ?: return false
        system.trust[trustee] = level
        updateSystem(system)
        return true
    }

    override suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel {
        return fetchSystemFromId(systemId)?.trust?.get(trustee) ?: TrustLevel.NONE
    }

    override suspend fun fetchTotalSystems(): Int {
        return systems.countDocuments().awaitFirst().toInt()
    }

    override suspend fun fetchTotalMembersFromSystem(systemId: String): Int {
        return members.countDocuments("{systemId:'$systemId'}").awaitFirst().toInt()
    }

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String): MemberRecord? {
        return members.findOne("{systemId:'$systemId',name:'${memberName.replace("'", "\\'")}'}")
    }

    override suspend fun export(other: Database) {
        TODO("Not yet implemented")
    }

    override fun close() {
        kmongo.close()
    }
}