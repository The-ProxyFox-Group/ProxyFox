package dev.proxyfox.database

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import dev.kord.common.entity.Snowflake
import dev.proxyfox.database.DatabaseUtil.findAll
import dev.proxyfox.database.DatabaseUtil.findOne
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
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.reactivestreams.countDocuments
import org.litote.kmongo.reactivestreams.deleteOne
import org.litote.kmongo.reactivestreams.deleteOneById
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

    suspend fun setup() {
        kmongo = KMongo.createClient()
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
    }

    override suspend fun getUser(userId: String): UserRecord {
        var user = users.findOne("{id:'$userId'}")
        if (user == null) {
            user = UserRecord()
            user.id = userId
            users.insertOne(user).awaitFirst()
        }
        return user
    }

    override suspend fun getSystemByHost(userId: String): SystemRecord? {
        val user = getUser(userId)
        return user.system?.let { getSystemById(it) }
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? = systems.findOne("{id:'$systemId'}")

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? {
        val user = getUser(userId)
        return user.system?.let { getMembersBySystem(it) }
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord> =
        members.findAll("{systemId:'$systemId'}")

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? {
        val user = getUser(userId)
        return user.system?.let { getMemberById(it, memberId) }
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? =
        members.findOne("{id:'$memberId', systemId:'$systemId'}")

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? {
        val user = getUser(userId)
        return user.system?.let { getFrontingMembersById(it) }
    }

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?> {
        val switch = systemSwitches.findAll("{systemId:'$systemId'}").minByOrNull {
            it.timestamp
        }!!
        val members = ArrayList<MemberRecord?>()
        for (memberId in switch.memberIds)
            members.add(getMemberById(systemId, memberId))
        return members
    }

    override suspend fun getProxiesByHost(userId: String): List<MemberProxyTagRecord>? {
        val user = getUser(userId)
        return user.system?.let { getProxiesById(it) }
    }

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId'}")

    override suspend fun getProxiesByHostAndMember(
        userId: String,
        memberId: String
    ): List<MemberProxyTagRecord>? {
        val user = getUser(userId)
        return user.system?.let { getProxiesByIdAndMember(it, memberId) }
    }

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun getMemberFromMessage(
        userId: String,
        message: String
    ): MemberRecord? {
        val proxyTag = getProxyTagFromMessage(userId, message) ?: return null
        return getMemberByHost(userId, proxyTag.memberId)
    }

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? {
        val proxies = getProxiesByHost(userId) ?: return null
        for (proxy in proxies)
            if (proxy.test(message)) return proxy
        return null
    }

    override suspend fun getMemberServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        val user = getUser(userId)
        return user.system?.let { getMemberServerSettingsById(serverId, it, memberId) }
    }

    override suspend fun getMemberServerSettingsById(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? =
        memberServers.findOne("{serverId:'$serverId',systemId:'$systemId',memberId:'$memberId'}")

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? {
        val user = getUser(userId)
        return user.system?.let { getServerSettingsById(serverId, it) }
    }

    override suspend fun getServerSettingsById(serverId: String, systemId: String): SystemServerSettingsRecord {
        var serverSettings = systemServers.findOne("{serverId:'$serverId',systemId:'$systemId'}")
        if (serverSettings == null) {
            serverSettings = SystemServerSettingsRecord()
            serverSettings.serverId = serverId
            serverSettings.systemId = systemId
            systemServers.insertOne(serverSettings).awaitFirst()
        }
        return serverSettings
    }

    override suspend fun getServerSettings(serverId: String): ServerSettingsRecord {
        var serverSettings = servers.findOne("{serverId:'$serverId'}")
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

    override suspend fun getChannelSettings(serverId: String, systemId: String): SystemChannelSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: String): SystemRecord {
        if (getSystemByHost(userId) != null) return getSystemByHost(userId)!!
        val user = getUser(userId)
        val systems = this.systems.find().toList()
        var currentId = 0
        for (system in systems) {
            if (system.id.fromPkString() > currentId + 1) break
            currentId = system.id.fromPkString()
        }
        val system = SystemRecord()
        system.id = (currentId + 1).toPkString()
        system.users.add(userId)
        user.system = system.id
        updateUser(user)
        this.systems.insertOne(system).awaitFirst()
        return system
    }

    override suspend fun removeSystem(userId: String): Boolean {
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

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord? {
        getSystemById(systemId) ?: return null
        val members = getMembersBySystem(systemId)
        var currentId = 0
        for (member in members) {
            if (member.name == name) return member
            if (member.id.fromPkString() > currentId + 1) break
            currentId = member.id.fromPkString()
        }
        val member = MemberRecord()
        member.id = (currentId + 1).toPkString()
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

    override suspend fun updateUser(user: UserRecord) {
        users.deleteOneById(user._id).awaitFirst()
        users.insertOne(user).awaitFirst()
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
        messages.insertOne(message).awaitFirst()
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if(prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        val proxyTags = getProxiesById(systemId)
        for (proxy in proxyTags)
            if (prefix == proxy.prefix && suffix == proxy.suffix) return null
        val proxy = MemberProxyTagRecord()
        proxy.prefix = prefix ?: ""
        proxy.suffix = suffix ?: ""
        proxy.memberId = memberId
        proxy.systemId = systemId
        memberProxies.insertOne(proxy).awaitFirst()
        return proxy
    }

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies.deleteOne("{systemId:'${proxyTag.systemId}',memberId:'${proxyTag.memberId}',prefix:'${proxyTag.prefix}',suffix:'${proxyTag.suffix}'}")
            .awaitFirst()
    }

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel): Boolean {
        val user = getUser(userId)
        user.trust[trustee] = level
        updateUser(user)
        return true
    }

    override suspend fun getTrustLevel(userId: String, trustee: String): TrustLevel {
        val user = getUser(userId)
        return user.trust[trustee] ?: TrustLevel.NONE
    }

    override suspend fun getTotalSystems(): Int {
        return systems.countDocuments().awaitFirst().toInt()
    }

    override suspend fun getTotalMembersByHost(userId: String): Int? {
        val user = getUser(userId)
        user.system ?: return null
        return getTotalMembersById(user.system!!)
    }

    override suspend fun getTotalMembersById(systemId: String): Int {
        return members.countDocuments("{systemId:'$systemId'}").awaitFirst().toInt()
    }

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? {
        return members.findOne("{systemId:'$systemId',name:'$memberName'}")
    }

    override suspend fun getMemberByHostAndName(userId: String, memberName: String): MemberRecord? {
        val user = getUser(userId)
        return user.system?.let { getMemberByIdAndName(it, memberName) }
    }

    override suspend fun export(other: Database) {
        TODO("Not yet implemented")
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
        kmongo.close()
    }
}