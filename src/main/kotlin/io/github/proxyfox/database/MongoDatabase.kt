package io.github.proxyfox.database

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.github.proxyfox.database.DatabaseUtil.findAll
import io.github.proxyfox.database.DatabaseUtil.findOne
import io.github.proxyfox.database.DatabaseUtil.fromPkString
import io.github.proxyfox.database.DatabaseUtil.getOrCreateCollection
import io.github.proxyfox.database.DatabaseUtil.toPkString
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.ChannelSettingsRecord
import io.github.proxyfox.database.records.misc.ServerSettingsRecord
import io.github.proxyfox.database.records.misc.TrustLevel
import io.github.proxyfox.database.records.misc.UserRecord
import io.github.proxyfox.database.records.system.SystemChannelSettingsRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.coroutines.reactive.awaitFirst
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.reactivestreams.countDocuments
import org.litote.kmongo.reactivestreams.deleteOne
import org.litote.kmongo.reactivestreams.findOneAndReplace

// Created 2022-26-05T22:43:40

typealias Mongo = com.mongodb.reactivestreams.client.MongoDatabase
typealias KCollection<T> = MongoCollection<T>

/**
 * @author Ampflower, Emma
 * @since ${version}
 **/
class MongoDatabase : Database() {
    private lateinit var kmongo: MongoClient
    private lateinit var db: Mongo

    private lateinit var users: KCollection<UserRecord>

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
            users.insertOne(user)
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

    override suspend fun getProxiesByHost(userId: String): Collection<MemberProxyTagRecord>? {
        val user = getUser(userId)
        return user.system?.let { getProxiesById(it) }
    }

    override suspend fun getProxiesById(systemId: String): Collection<MemberProxyTagRecord> =
        memberProxies.findAll("{systemId:'$systemId'}")

    override suspend fun getProxiesByHostAndMember(
        userId: String,
        memberId: String
    ): Collection<MemberProxyTagRecord>? {
        val user = getUser(userId)
        return user.system?.let { getProxiesByIdAndMember(it, memberId) }
    }

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): Collection<MemberProxyTagRecord> =
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

    override suspend fun getServerSettingsById(serverId: String, systemId: String): SystemServerSettingsRecord? =
        systemServers.findOne("{serverId:'$serverId',systemId:'$systemId'}")

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
        this.systems.insertOne(system)
        db.return system
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
        this.members.insertOne(member)
        return member
    }

    override suspend fun updateMember(member: MemberRecord) {
        members.findOneAndReplace("{systemId:'${member.systemId}',id:'${member.id}'}", member)
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        memberServers.findOneAndReplace(
            "{memberId:'${serverSettings.memberId}',systemId:'${serverSettings.systemId}',serverId:'${serverSettings.serverId}'}",
            serverSettings
        )
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems.findOneAndReplace("{id:'${system.id}'}", system)
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systemServers.findOneAndReplace(
            "{systemId:'${serverSettings.systemId}',serverId:'${serverSettings.serverId}'}",
            serverSettings
        )
    }

    override suspend fun updateUser(user: UserRecord) {
        users.findOneAndReplace("{id:'${user.id}'}", user)
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        val proxyTags = getProxiesById(systemId)
        for (proxy in proxyTags)
            if (prefix == proxy.prefix && suffix == proxy.suffix) return null
        val proxy = MemberProxyTagRecord()
        proxy.prefix = prefix
        proxy.suffix = suffix
        proxy.memberId = memberId
        proxy.systemId = systemId
        memberProxies.insertOne(proxy)
        return proxy
    }

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies.deleteOne("{systemId:'${proxyTag.systemId}',memberId:'${proxyTag.memberId}',prefix:'${proxyTag.prefix}',suffix:'${proxyTag.suffix}'")
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
        return members.findOne("{systemId:'$systemId',name:$memberName}")
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