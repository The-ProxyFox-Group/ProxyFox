package io.github.proxyfox.database

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import io.github.proxyfox.database.DatabaseUtil.findAll
import io.github.proxyfox.database.DatabaseUtil.findOne
import io.github.proxyfox.database.DatabaseUtil.getOrCreateCollection
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
import org.litote.kmongo.reactivestreams.KMongo

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

    override suspend fun getUser(userId: String): UserRecord? {
        return users.findOne("{id='$userId'}")
    }

    override suspend fun getSystemByHost(userId: String): SystemRecord? {
        val user = getUser(userId) ?: return null
        return user.system?.let { getSystemById(it) }
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? = systems.findOne("{id='$systemId'}")

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? {
        val user = getUser(userId) ?: return null
        return user.system?.let { getMembersBySystem(it) }
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? =
        members.findAll("{systemId='$systemId'}")

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? {
        val user = getUser(userId) ?: return null
        return user.system?.let { getMemberById(it, memberId) }
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? =
        members.findOne("{id='$memberId', systemId='$systemId'}")

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? {
        val user = getUser(userId) ?: return null
        return user.system?.let { getFrontingMembersById(it) }
    }

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?>? {
        val switch = systemSwitches.findAll("{systemId='$systemId'}").minByOrNull {
            it.timestamp
        }!!
        val members = ArrayList<MemberRecord?>()
        for (memberId in switch.memberIds)
            members.add(getMemberById(systemId, memberId))
        return members
    }

    override suspend fun getFrontingMemberByTags(
        userId: String,
        message: String
    ): Pair<MemberRecord, MemberProxyTagRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingServerSettingsByHost(
        serverId: String,
        userId: String
    ): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByMember(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: String): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord {
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

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String,
        suffix: String
    ): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun removeProxyTag(systemId: String, proxyTag: MemberProxyTagRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun addUserToSystem(userId: String, systemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserFromSystem(userId: String, systemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel) {
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