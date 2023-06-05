/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.mongodb.client.model.*
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.Channel
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.group.GroupRecord
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.or
import org.litote.kmongo.path
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.util.KMongoUtil
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty0
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


// Created 2022-26-05T22:43:40

typealias Mongo = com.mongodb.reactivestreams.client.MongoDatabase
typealias KCollection<T> = MongoCollection<T>

/**
 * @author Ampflower, Emma
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
    private lateinit var systemTokens: KCollection<TokenRecord>

    private lateinit var systemServers: KCollection<SystemServerSettingsRecord>
    private lateinit var systemChannels: KCollection<SystemChannelSettingsRecord>

    private lateinit var members: KCollection<MemberRecord>
    private lateinit var memberProxies: KCollection<MemberProxyTagRecord>

    private lateinit var memberServers: KCollection<MemberServerSettingsRecord>

    private lateinit var groups: KCollection<GroupRecord>

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
        systemTokens = db.getOrCreateCollection()
        systemSwitches = db.getOrCreateCollection()

        systemServers = db.getOrCreateCollection()
        systemChannels = db.getOrCreateCollection()

        members = db.getOrCreateCollection()
        memberProxies = db.getOrCreateCollection()

        memberServers = db.getOrCreateCollection()

        groups = db.getOrCreateCollection()

        ping()

        return this
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun ping(): Duration {
        return measureTime {
            db.runCommand<JsonObject>("{ping: 1}").awaitFirst()
        }
    }

    override suspend fun getDatabaseName() = "MongoDB"

    override suspend fun fetchUser(userId: ULong): UserRecord? {
        return users.findFirstOrNull("id" eq userId)
    }

    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? =
        systems.findFirstOrNull("id" eq systemId)

    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord> =
        members.findList("systemId" eq systemId)

    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? {
        return members.findFirstOrNull("id" eq memberId, "systemId" eq systemId)
    }

    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord> =
        memberProxies.findList("systemId" eq systemId)

    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord> =
        memberProxies.findList("systemId" eq systemId, "memberId" eq memberId)

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord =
        memberServers.findFirstOrNull("serverId" eq serverId, "systemId" eq systemId, "memberId" eq memberId)
            ?: MemberServerSettingsRecord(
                serverId = serverId,
                systemId = systemId,
                memberId = memberId,
            )

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        return systemServers.findFirstOrNull("serverId" eq serverId, "systemId" eq systemId)
            ?: SystemServerSettingsRecord(serverId, systemId)
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord =
        servers.findFirstOrNull("serverId" eq serverId) ?: ServerSettingsRecord(serverId)

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        servers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord =
        systemChannels.findFirstOrNull("channelId" eq channelId, "systemId" eq systemId)
            ?: SystemChannelSettingsRecord(channelId, systemId)

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord =
        channels.findFirstOrNull("channelId" eq channelId)
            ?: ChannelSettingsRecord(serverId, channelId)

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        channels.replaceOneById(channel._id, channel, upsert()).awaitFirst()
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        return fetchSystemFromUser(userId) ?: run {
            val user = getOrCreateUser(userId)
            val system = SystemRecord()
            system.id = firstFreeSystemId(id)
            system.users.add(userId)
            user.systemId = system.id
            updateUser(user)
            this.systems.insertOne(system).awaitFirst()
            system
        }
    }

    override suspend fun dropSystem(userId: ULong): Boolean {
        val system = fetchSystemFromUser(userId) ?: return false
        val filter = "systemId" eq system.id
        systemServers.deleteMany(filter).awaitFirst()
        systemChannels.deleteMany(filter).awaitFirst()
        systemSwitches.deleteMany(filter).awaitFirst()
        memberProxies.deleteMany(filter).awaitFirst()
        memberServers.deleteMany(filter).awaitFirst()
        members.deleteMany(filter).awaitFirst()
        dropTokens(system.id)
        systems.deleteOneById(system._id).awaitFirst()
        users.deleteMany(filter).awaitFirst()
        return true
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        val member = fetchMemberFromSystem(systemId, memberId) ?: return false
        val filter = and("systemId" eq systemId, "memberId" eq memberId)
        memberProxies.deleteMany(filter).awaitFirst()
        memberServers.deleteMany(filter).awaitFirst()
        members.deleteOneById(member._id).awaitFirst()
        return true
    }

    override suspend fun updateMember(member: MemberRecord) {
        members.replaceOneById(member._id, member, upsert()).awaitFirst()
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        memberServers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems.replaceOneById(system._id, system, upsert()).awaitFirst()
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systemServers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
    }

    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        systemChannels.replaceOneById(channelSettings._id, channelSettings, upsert()).awaitFirst()
    }

    override suspend fun updateUser(user: UserRecord) {
        users.replaceOneById(user._id, user, upsert()).awaitFirst()
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
        val channel = channelBehavior.asChannelOf<Channel>()
        message.guildId = channel.data.guildId.value?.value ?: 0UL
        message.channelId = channel.id.value
        message.memberId = memberId
        message.systemId = systemId
        messages.insertOne(message).awaitFirst()
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        messages.replaceOneById(message._id, message, upsert()).awaitFirst()
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? =
            messages.findFirstOrNull(or(
                    "newMessageId" eq messageId,
                    "oldMessageId" eq messageId,
                    "deleted" eq false
            ))

    override suspend fun fetchLatestMessage(
            systemId: String,
            channelId: Snowflake
    ): ProxiedMessageRecord? =
            messages.find(
                    "systemId" eq systemId,
                    "channelId" eq channelId,
                    "deleted" eq false
            ).sort("{'creationDate':-1}").limit(1)
                    .awaitFirstOrNull()

    override suspend fun dropMessage(messageId: Snowflake) {
        messages.deleteOne("oldMessageId" eq messageId.value)
    }

    override suspend fun fetchToken(token: String): TokenRecord? =
        systemTokens.findFirstOrNull("token" eq token)

    override suspend fun fetchTokenFromId(systemId: String, id: String): TokenRecord? =
        systemTokens.findFirstOrNull("systemId" eq systemId, "id" eq id)

    override suspend fun fetchTokens(systemId: String): List<TokenRecord> =
        systemTokens.find("systemId" eq systemId).toList()

    override suspend fun updateToken(token: TokenRecord) {
        systemTokens.replaceOneById(token._id, token, upsert()).awaitFirst()
    }

    override suspend fun dropToken(token: String) {
        systemTokens.deleteOne("token" eq token).awaitFirst()
    }

    override suspend fun dropTokenById(systemId: String, id: String) {
        systemTokens.deleteOne("systemId" eq systemId, "id" eq id).awaitFirst()
    }

    override suspend fun dropTokens(systemId: String) {
        systemTokens.deleteMany("systemId" eq systemId).awaitFirst()
    }

    override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
        memberProxies.insertOne(record).awaitFirst()
        return true
    }

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord? {
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
        systemSwitches.replaceOneById(switch._id, switch, upsert()).awaitFirst()
    }

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord> =
        systemSwitches.find("systemId" eq systemId).toList()

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies.deleteOne(and(proxyTag::systemId, proxyTag::memberId, proxyTag::prefix, proxyTag::suffix)).awaitFirst()
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
        return members.countDocuments("systemId" eq systemId).awaitFirst().toInt()
    }

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String, caseSensitive: Boolean): MemberRecord? {
        var search = members.find(
            "systemId" eq systemId,
            "name" eq memberName,
        )
        if (!caseSensitive) search = search.collation(Collation.builder().apply {
            collationStrength(CollationStrength.SECONDARY)
            caseLevel(false)
            locale("en_US")
        }.build())
        return search.awaitFirstOrNull()
    }

    override suspend fun fetchGroupsFromMember(member: MemberRecord): List<GroupRecord> {
        return groups.find(
            "systemId" eq member.systemId,
            "members" eq member.id
        ).toList()
    }

    override suspend fun fetchMembersFromGroup(group: GroupRecord): List<MemberRecord> {
        val out = arrayListOf<MemberRecord>()
        group.members.forEach {
            out.add(fetchMemberFromSystem(group.systemId, it) ?: return@forEach)
        }
        return out
    }

    override suspend fun fetchGroupFromSystem(system: PkId, groupId: String): GroupRecord? {
        return groups.find(
            "systemId" eq system,
            "id" eq groupId
        ).awaitFirstOrNull()
    }

    override suspend fun fetchGroupsFromSystem(system: PkId): List<GroupRecord>? {
        if (!containsSystem(system)) return null
        return groups.find(
            "systemId" eq system,
        ).toList()
    }

    override suspend fun fetchGroupFromSystemAndName(
        system: PkId,
        name: String,
        caseSensitive: Boolean
    ): GroupRecord? {
        var search = groups.find(
            "systemId" eq system,
            "name" eq name
        )
        if (!caseSensitive) search = search.collation(Collation.builder().apply {
            collationStrength(CollationStrength.SECONDARY)
            caseLevel(false)
            locale("en_US")
        }.build())
        return search.awaitFirstOrNull()
    }

    override suspend fun updateGroup(group: GroupRecord) {
        groups.replaceOneById(group._id, group, upsert()).awaitFirst()
    }

    override suspend fun export(other: Database) {
        TODO("Not yet implemented")
    }

    override fun bulkInserter(): Database {
        return BulkInserter(this)
    }

    @Deprecated("Not for regular use.", level = DeprecationLevel.ERROR)
    override suspend fun drop() {
        db.drop().awaitFirstOrNull()
        kmongo.close()
    }

    override suspend fun firstFreeSystemId(id: String?): String {
        return if (isSystemIdReserved(id)) systems.find().toList().map(SystemRecord::id).firstFree() else id
    }

    override fun close() {
        kmongo.close()
    }

    private class BulkInserter(mongo: MongoDatabase) : ProxyDatabase<MongoDatabase>(mongo) {
        private val logger = LoggerFactory.getLogger(BulkInserter::class.java)

        private val serverSettingsQueue = ConcurrentLinkedQueue<WriteModel<ServerSettingsRecord>>()
        private val channelSettingsQueue = ConcurrentLinkedQueue<WriteModel<ChannelSettingsRecord>>()
        private val memberQueue = ConcurrentLinkedQueue<WriteModel<MemberRecord>>()
        private val memberServerSettingsQueue = ConcurrentLinkedQueue<WriteModel<MemberServerSettingsRecord>>()
        private val systemQueue = ConcurrentLinkedQueue<WriteModel<SystemRecord>>()
        private val systemServerSettingsQueue = ConcurrentLinkedQueue<WriteModel<SystemServerSettingsRecord>>()
        private val systemChannelSettingsQueue = ConcurrentLinkedQueue<WriteModel<SystemChannelSettingsRecord>>()
        private val userQueue = ConcurrentLinkedQueue<WriteModel<UserRecord>>()
        private val proxiedMessageQueue = ConcurrentLinkedQueue<WriteModel<ProxiedMessageRecord>>()
        private val systemSwitchQueue = ConcurrentLinkedQueue<WriteModel<SystemSwitchRecord>>()
        private val memberProxiesQueue = ConcurrentLinkedQueue<WriteModel<MemberProxyTagRecord>>()
        private val witness = HashSet<Any?>()

        override suspend fun getDatabaseName(): String {
            return proxy.getDatabaseName() + " (Bulk Inserter)"
        }

        override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
            if (witness.add(serverSettings)) serverSettingsQueue += serverSettings.replace()
        }

        override suspend fun updateChannel(channel: ChannelSettingsRecord) {
            if (witness.add(channel)) channelSettingsQueue += channel.replace()
        }

        override suspend fun updateMember(member: MemberRecord) {
            if (witness.add(member)) memberQueue += member.replace()
        }

        override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
            if (witness.add(serverSettings)) memberServerSettingsQueue += serverSettings.replace()
        }

        override suspend fun updateSystem(system: SystemRecord) {
            if (witness.add(system)) systemQueue += system.replace()
        }

        override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
            if (witness.add(serverSettings)) systemServerSettingsQueue += serverSettings.replace()
        }

        override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
            if (witness.add(channelSettings)) systemChannelSettingsQueue += channelSettings.replace()
        }

        override suspend fun updateUser(user: UserRecord) {
            if (witness.add(user)) userQueue += user.replace()
        }

        override suspend fun updateMessage(message: ProxiedMessageRecord) {
            if (witness.add(message)) proxiedMessageQueue += message.replace()
        }

        override suspend fun createServerSettings(serverSettings: ServerSettingsRecord) {
            if (witness.add(serverSettings)) serverSettingsQueue += serverSettings.create()
        }

        override suspend fun createChannel(channel: ChannelSettingsRecord) {
            if (witness.add(channel)) channelSettingsQueue += channel.create()
        }

        override suspend fun createMember(member: MemberRecord) {
            if (witness.add(member)) memberQueue += member.create()
        }

        override suspend fun createMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
            if (witness.add(serverSettings)) memberServerSettingsQueue += serverSettings.create()
        }

        override suspend fun createSystem(system: SystemRecord) {
            if (witness.add(system)) systemQueue += system.create()
        }

        override suspend fun createSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
            if (witness.add(serverSettings)) systemServerSettingsQueue += serverSettings.create()
        }

        override suspend fun createSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
            if (witness.add(channelSettings)) systemChannelSettingsQueue += channelSettings.create()
        }

        override suspend fun createUser(user: UserRecord) {
            if (witness.add(user)) userQueue += user.create()
        }

        override suspend fun createMessage(message: ProxiedMessageRecord) {
            if (witness.add(message)) proxiedMessageQueue += message.create()
        }

        override suspend fun updateSwitch(switch: SystemSwitchRecord) {
            if (witness.add(switch)) systemSwitchQueue += switch.replace()
        }

        override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
            if (witness.add(record)) memberProxiesQueue += record.create()
            return true
        }

        override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord {
            val record = SystemSwitchRecord(systemId, "", memberId, timestamp)
            systemSwitchQueue += record.create()
            return record
        }

        override suspend fun createSwitch(switch: SystemSwitchRecord) {
            if (witness.add(switch)) systemSwitchQueue += switch.create()
        }

        override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
            logger.warn(
                "Performance degradation: #updateTrustLevel called on bulkInserter for {}: {} -> {}",
                systemId, trustee, level, Throwable("Debug trace.")
            )

            commit()
            return super.updateTrustLevel(systemId, trustee, level)
        }

        override suspend fun dropMember(systemId: String, memberId: String): Boolean {
            val member = fetchMemberFromSystem(systemId, memberId) ?: return false
            val filter = KMongoUtil.toBson("{systemId:'$systemId',memberId:'$memberId'}")
            memberProxiesQueue += DeleteManyModel(filter)
            memberServerSettingsQueue += DeleteManyModel(filter)
            memberQueue += member.delete()
            return true
        }

        override suspend fun dropSwitch(switch: SystemSwitchRecord) {
            systemSwitchQueue += switch.delete()
        }

        override suspend fun dropSystem(userId: ULong): Boolean {
            val system = fetchSystemFromUser(userId) ?: return false
            val filter = KMongoUtil.toBson("{systemId:'${system.id}'}")
            systemServerSettingsQueue += DeleteManyModel(filter)
            systemChannelSettingsQueue += DeleteManyModel(filter)
            systemSwitchQueue += DeleteManyModel(filter)
            memberProxiesQueue += DeleteManyModel(filter)
            memberServerSettingsQueue += DeleteManyModel(filter)
            memberQueue += DeleteManyModel(filter)
            systemQueue += system.delete()
            userQueue += DeleteManyModel(filter)
            dropTokens(system.id)
            return true
        }

        override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
            memberProxiesQueue += proxyTag.delete()
        }

        override suspend fun commit() {
            proxy.servers.bulkWrite(serverSettingsQueue)
            proxy.channels.bulkWrite(channelSettingsQueue)
            proxy.members.bulkWrite(memberQueue)
            proxy.memberServers.bulkWrite(memberServerSettingsQueue)
            proxy.systems.bulkWrite(systemQueue)
            proxy.systemServers.bulkWrite(systemServerSettingsQueue)
            proxy.systemChannels.bulkWrite(systemChannelSettingsQueue)
            proxy.users.bulkWrite(userQueue)
            proxy.messages.bulkWrite(proxiedMessageQueue)
            proxy.systemSwitches.bulkWrite(systemSwitchQueue)
            proxy.memberProxies.bulkWrite(memberProxiesQueue)
            witness.clear()
        }

        private suspend fun <T> KCollection<T>.bulkWrite(collection: MutableCollection<WriteModel<T>>) {
            if (collection.isNotEmpty()) {
                bulkWrite(collection.toList()).awaitFirstOrNull()
                collection.clear()
            }
        }

        private fun <T : MongoRecord> T.delete() = DeleteOneModel<T>(Filters.eq("_id", _id))
        private fun <T : MongoRecord> T.upsert() = ReplaceOneModel(Filters.eq("_id", _id), this, ReplaceOptions().upsert(true))
        private fun <T : MongoRecord> T.replace() = ReplaceOneModel(Filters.eq("_id", _id), this)
        private fun <T : Any> T.create() = InsertOneModel(this)
    }

    private suspend inline fun <T> KCollection<T>.findFirst(filter: Bson): T = find(filter).awaitFirst()

    private suspend inline fun <T> KCollection<T>.findFirstOrNull(filter: Bson): T? = find(filter).awaitFirstOrNull()

    private suspend inline fun <T> KCollection<T>.findList(filter: Bson): List<T> = find(filter).toList()

    private suspend inline fun <T> KCollection<T>.findFirst(vararg filter: Bson): T = find(*filter).awaitFirst()

    private suspend inline fun <T> KCollection<T>.findFirstOrNull(vararg filter: Bson): T? = find(*filter).awaitFirstOrNull()

    private suspend inline fun <T> KCollection<T>.findFirstOrElse(vararg filter: Bson, noinline action: () -> T): T = find(*filter).awaitFirstOrElse(action)

    private suspend inline fun <T> KCollection<T>.findList(vararg filter: Bson): List<T> = find(*filter).toList()

    private fun upsert() = ReplaceOptions().upsert(true)

    private infix fun <T> String.eq(t: T) = Filters.eq(this, t)

    private infix fun String.eq(t: ULong) = Filters.eq(this, t.toLong())

    private infix fun String.eq(t: Snowflake) = Filters.eq(this, t.value.toLong())

    private fun <T> KProperty0<T>.eq() = Filters.eq(path(), get())

    private fun and(vararg properties: KProperty0<*>) = Filters.and(properties.map { it.eq() })

    private fun or(vararg properties: KProperty0<*>) = Filters.or(properties.map { it.eq() })
}