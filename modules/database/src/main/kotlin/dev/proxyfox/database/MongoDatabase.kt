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
import dev.proxyfox.database.DatabaseUtil.findAll
import dev.proxyfox.database.DatabaseUtil.findOne
import dev.proxyfox.database.DatabaseUtil.firstFree
import dev.proxyfox.database.DatabaseUtil.fromPkString
import dev.proxyfox.database.DatabaseUtil.getOrCreateCollection
import dev.proxyfox.database.DatabaseUtil.toPkString
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
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit


// Created 2022-26-05T22:43:40

typealias Mongo = com.mongodb.reactivestreams.client.MongoDatabase
typealias KCollection<T> = MongoCollection<T>

/**
 * @author KJP12, Emma
 * @since ${version}
 **/
class MongoDatabase : Database() {
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
        db = kmongo.getDatabase("ProxyFox")

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

    override suspend fun getUser(userId: ULong): UserRecord {
        var user = users.findOne("{id:$userId}")
        if (user == null) {
            user = UserRecord()
            user.id = userId
            users.insertOne(user).awaitFirst()
        }
        return user
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? = systems.findOne("{id:'$systemId'}")

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord> =
        members.findAll("{systemId:'$systemId'}")

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        return members.findOne("{id:'$memberId', systemId:'$systemId'}")
    }

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId'}")

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun getMemberServerSettingsById(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord =
        memberServers.findOne("{serverId:$serverId,systemId:'$systemId',memberId:'$memberId'}") ?: MemberServerSettingsRecord().apply {
            this.serverId = serverId
            this.systemId = systemId
            this.memberId = memberId
        }

    override suspend fun getServerSettingsById(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        var serverSettings = systemServers.findOne("{serverId:$serverId,systemId:'$systemId'}")
        if (serverSettings == null) {
            serverSettings = SystemServerSettingsRecord()
            serverSettings.serverId = serverId
            serverSettings.systemId = systemId
            systemServers.insertOne(serverSettings).awaitFirst()
        }
        return serverSettings
    }

    override suspend fun getServerSettings(serverId: ULong): ServerSettingsRecord {
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

    override suspend fun getChannelSettings(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
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

    override suspend fun allocateSystem(userId: ULong, id: String?): SystemRecord {
        if (getSystemByHost(userId) != null) return getSystemByHost(userId)!!
        var nid = id
        if (id != null && getSystemById(id) != null) nid = null
        val user = getUser(userId)
        val system = SystemRecord()
        system.id = nid ?: systems.find().toList().map(SystemRecord::id).firstFree()
        system.users.add(userId)
        user.system = system.id
        updateUser(user)
        this.systems.insertOne(system).awaitFirst()
        return system
    }

    override suspend fun removeSystem(userId: ULong): Boolean {
        val system = getSystemByHost(userId) ?: return false
        systemServers.findAll("{systemId:'${system.id}'}").forEach {
            systemServers.deleteOneById(it._id).awaitFirst()
        }
        systemChannels.findAll("{systemId:'${system.id}'}").forEach {
            systemChannels.deleteOneById(it._id).awaitFirst()
        }
        getMembersBySystem(system.id).forEach {
            removeMember(system.id, it.id)
        }
        systems.deleteOneById(system._id).awaitFirst()
        val user = getUser(userId)
        users.deleteOneById(user._id).awaitFirst()
        return true
    }

    override suspend fun allocateMember(systemId: String, name: String, id: String?): MemberRecord? {
        getSystemById(systemId) ?: return null
        var nid = id
        if (id != null && getMemberById(systemId, id) != null) nid = null
        val member = MemberRecord()
        member.id = nid ?: getMembersBySystem(systemId).map(MemberRecord::id).firstFree()
        member.name = name
        member.systemId = systemId
        this.members.insertOne(member).awaitFirst()
        return member
    }

    override suspend fun removeMember(systemId: String, memberId: String): Boolean {
        val member = getMemberById(systemId, memberId) ?: return false
        getProxiesByIdAndMember(systemId, memberId).forEach {
            memberProxies.deleteOneById(it._id).awaitFirst()
        }
        memberServers.findAll("{systemId:'$systemId',memberId:'${member.id}'}").forEach {
            memberServers.deleteOneById(it._id).awaitFirst()
        }
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
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        channelId: Snowflake,
        memberId: String,
        systemId: String
    ) {
        val message = ProxiedMessageRecord()
        message.oldMessageId = oldMessageId.value
        message.newMessageId = newMessageId.value
        message.channelId = channelId.value
        message.memberId = memberId
        message.systemId = systemId
        messages.insertOne(message).awaitFirst()
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? =
        messages.findOne("{\$or:[{'newMessageId':$messageId},{'oldMessageId':$messageId}]}")

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? =
        messages.find().filter("{'systemId':'$systemId','channelId':$channelId}").sort("{'creationDate':-1}").limit(1).awaitFirstOrNull()

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if (prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        getProxiesById(systemId).firstOrNull { prefix == it.prefix && suffix == it.suffix }?.let {
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

    override suspend fun listProxyTags(systemId: String, memberId: String) =
        memberProxies.findAll("{systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun allocateSwitch(systemId: String, memberId: List<String>, timestamp: OffsetDateTime?): SystemSwitchRecord? {
        getSystemById(systemId) ?: return null
        val switches = getSwitchesById(systemId)
        val switch = SystemSwitchRecord()
        val id = ((switches.maxOfOrNull { it.id.fromPkString() } ?: 0) + 1).toPkString()
        switch.id = id
        switch.systemId = systemId
        switch.memberIds = memberId
        timestamp?.let { switch.timestamp = it }
        systemSwitches.insertOne(switch).awaitFirst()
        return switch
    }

    override suspend fun getLatestSwitch(systemId: String) =
        systemSwitches.findAll("{systemId:'$systemId'}").maxByOrNull {
            it.timestamp
        }

    override suspend fun getSwitchesById(systemId: String): List<SystemSwitchRecord> =
        systemSwitches.findAll("{systemId:'$systemId'}")

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies.deleteOne("{systemId:'${proxyTag.systemId}',memberId:'${proxyTag.memberId}',prefix:'${proxyTag.prefix}',suffix:'${proxyTag.suffix}'}")
            .awaitFirst()
    }

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
        val system = getSystemById(systemId) ?: return false
        system.trust[trustee] = level
        updateSystem(system)
        return true
    }

    override suspend fun getTrustLevel(systemId: String, trustee: ULong): TrustLevel {
        return getSystemById(systemId)?.trust?.get(trustee) ?: TrustLevel.NONE
    }

    override suspend fun getTotalSystems(): Int {
        return systems.countDocuments().awaitFirst().toInt()
    }

    override suspend fun getTotalMembersById(systemId: String): Int {
        return members.countDocuments("{systemId:'$systemId'}").awaitFirst().toInt()
    }

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? {
        return members.findOne("{systemId:'$systemId',name:'${memberName.replace("'", "\\'")}'}")
    }

    override suspend fun export(other: Database) {
        TODO("Not yet implemented")
    }

    override fun close() {
        kmongo.close()
    }
}