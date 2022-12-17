/*
 * Copyright (c) 2022, The ProxyFox Group
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
import dev.proxyfox.database.records.MongoRecord
import dev.proxyfox.database.records.Record
import dev.proxyfox.database.records.member.*
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.*
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

    private lateinit var users: KCollection<MongoUserRecord>

    private lateinit var messages: KCollection<MongoProxiedMessageRecord>

    private lateinit var servers: KCollection<MongoServerSettingsRecord>
    private lateinit var channels: KCollection<MongoChannelSettingsRecord>

    private lateinit var systems: KCollection<MongoSystemRecord>
    private lateinit var systemSwitches: KCollection<MongoSystemSwitchRecord>
    private lateinit var systemTokens: KCollection<MongoTokenRecord>

    private lateinit var systemServers: KCollection<MongoSystemServerSettingsRecord>
    private lateinit var systemChannels: KCollection<MongoSystemChannelSettingsRecord>

    private lateinit var members: KCollection<MongoMemberRecord>
    private lateinit var memberProxies: KCollection<MongoMemberProxyTagRecord>

    private lateinit var memberServers: KCollection<MongoMemberServerSettingsRecord>

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
        if (serverSettings is MongoServerSettingsRecord) {
            servers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
        } else {
            val id = servers.findFirstOrNull("serverId" eq serverSettings.serverId)?._id
            if (id != null) {
                servers.replaceOneById(id, serverSettings.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord =
        systemChannels.findFirstOrNull("channelId" eq channelId, "systemId" eq systemId)
            ?: SystemChannelSettingsRecord(channelId, systemId)

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord =
        channels.findFirstOrNull("channelId" eq channelId)
            ?: ChannelSettingsRecord(serverId, channelId)

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        if (channel is MongoChannelSettingsRecord) {
            channels.replaceOneById(channel._id, channel, upsert()).awaitFirst()
        } else {
            val id = channels.findFirstOrNull("channelId" eq  channel.channelId)?._id
            if (id != null) {
                channels.replaceOneById(id, channel.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        return fetchSystemFromUser(userId) ?: run {
            val user = getOrCreateUser(userId)
            val system = SystemRecord()
            system.id = firstFreeSystemId(id)
            system.users.add(userId)
            user.systemId = system.id
            updateUser(user)
            this.systems.insertOne(MongoSystemRecord(system)).awaitFirst()
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
        if (system is MongoSystemRecord) {
            systems.deleteOneById(system._id).awaitFirst()
        } else {
            val id = systems.findFirstOrNull("systemId" eq system.id)?._id
            if (id != null) {
                systems.deleteOneById(id).awaitFirst()
            }
        }
        users.deleteMany(filter).awaitFirst()
        return true
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        val member = fetchMemberFromSystem(systemId, memberId) ?: return false
        val filter = and("systemId" eq systemId, "memberId" eq memberId)
        memberProxies.deleteMany(filter).awaitFirst()
        memberServers.deleteMany(filter).awaitFirst()
        if (member is MongoMemberRecord) {
            members.deleteOneById(member._id).awaitFirst()
        } else {
            val id = members.findFirstOrNull("memberId" eq member.id)?._id
            if (id != null) {
                members.deleteOneById(id).awaitFirst()
            }
        }
        return true
    }

    override suspend fun updateMember(member: MemberRecord) {
        if (member is MongoMemberRecord) {
            members.replaceOneById(member._id, member, upsert()).awaitFirst()
        } else {
            val id = members.findFirstOrNull("id" eq  member.id)?._id
            if (id != null) {
                members.replaceOneById(id, member.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        if (serverSettings is MongoMemberServerSettingsRecord) {
            memberServers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
        } else {
            val id = memberServers.findFirstOrNull("memberId" eq  serverSettings.memberId, "serverId" eq serverSettings.serverId)?._id
            if (id != null) {
                memberServers.replaceOneById(id, serverSettings.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun updateSystem(system: SystemRecord) {
        if (system is MongoSystemRecord) {
            systems.replaceOneById(system._id, system, upsert()).awaitFirst()
        } else {
            val id = systems.findFirstOrNull("systemId" eq system.id)?._id
            if (id != null) {
                systems.replaceOneById(id, system.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        if (serverSettings is MongoSystemServerSettingsRecord) {
            systemServers.replaceOneById(serverSettings._id, serverSettings, upsert()).awaitFirst()
        } else {
            val id = systemServers.findFirstOrNull("systemId" eq serverSettings.systemId, "serverId" eq serverSettings.serverId)?._id
            if (id != null) {
                systemServers.replaceOneById(id, serverSettings.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        if (channelSettings is MongoSystemChannelSettingsRecord) {
            systemChannels.replaceOneById(channelSettings._id, channelSettings, upsert()).awaitFirst()
        } else {
            val id = systemChannels.findFirstOrNull("systemId" eq channelSettings.systemId, "serverId" eq channelSettings.serverId)?._id
            if (id != null) {
                systemChannels.replaceOneById(id, channelSettings.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun updateUser(user: UserRecord) {
        if (user is MongoUserRecord) {
            users.replaceOneById(user._id, user, upsert()).awaitFirst()
        } else {
            val id = users.findFirstOrNull("userId" eq user.id)?._id
            if (id != null) {
                users.replaceOneById(id, user.toMongo(), upsert()).awaitFirst()
            }
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
        message.userId = userId.value
        message.oldMessageId = oldMessageId.value
        message.newMessageId = newMessageId.value
        val channel = channelBehavior.fetchChannel()
        message.guildId = channel.data.guildId.value?.value ?: 0UL
        message.channelId = channel.id.value
        message.memberId = memberId
        message.systemId = systemId
        messages.insertOne(message.toMongo()).awaitFirst()
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        if (message is MongoProxiedMessageRecord) {
            messages.replaceOneById(message._id, message, upsert()).awaitFirst()
        } else {
            val id = messages.findFirstOrNull("oldMessageId" eq  message.oldMessageId)?._id
            if (id != null) {
                messages.replaceOneById(id, message.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? =
        messages.findFirstOrNull(or("newMessageId" eq messageId, "oldMessageId" eq messageId))

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? =
        messages.find("systemId" eq systemId, "channelId" eq channelId).sort("{'creationDate':-1}").limit(1).awaitFirstOrNull()

    override suspend fun getOrCreateTokenFromSystem(systemId: String): TokenRecord =
        systemTokens.findFirstOrNull("systemId" eq systemId) ?: TokenRecord(generateToken(), systemId)

    override suspend fun updateToken(token: TokenRecord) {
        if (token is MongoTokenRecord) {
            systemTokens.replaceOneById(token._id, token, upsert()).awaitFirst()
        } else {
            val id = systemTokens.findFirstOrNull("token" eq  token.token)?._id
            if (id != null) {
                systemTokens.replaceOneById(id, token.toMongo(), upsert()).awaitFirst()
            }
        }
    }

    override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
        memberProxies.insertOne(record.toMongo()).awaitFirst()
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
        systemSwitches.insertOne(switch.toMongo()).awaitFirst()
        return switch
    }

    override suspend fun dropSwitch(switch: SystemSwitchRecord) {
        if (switch is MongoSystemSwitchRecord) {
            systemSwitches.deleteOneById(switch._id).awaitFirst()
        } else {
            val id = systemSwitches.findFirstOrNull("id" eq  switch.id, "systemId" eq switch.systemId)?._id
            if (id != null) {
                systemSwitches.deleteOneById(id).awaitFirst()
            }
        }
    }

    override suspend fun updateSwitch(switch: SystemSwitchRecord) {
        if (switch is MongoSystemSwitchRecord) {
            systemSwitches.replaceOneById(switch._id, switch.toMongo(), upsert()).awaitFirst()
        } else {
            val id = systemSwitches.findFirstOrNull("id" eq  switch.id, "systemId" eq switch.systemId)?._id
            if (id != null) {
                systemSwitches.replaceOneById(id, switch.toMongo(), upsert()).awaitFirst()
            }
        }
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

        private val serverSettingsQueue = ConcurrentLinkedQueue<WriteModel<MongoServerSettingsRecord>>()
        private val channelSettingsQueue = ConcurrentLinkedQueue<WriteModel<MongoChannelSettingsRecord>>()
        private val memberQueue = ConcurrentLinkedQueue<WriteModel<MongoMemberRecord>>()
        private val memberServerSettingsQueue = ConcurrentLinkedQueue<WriteModel<MongoMemberServerSettingsRecord>>()
        private val systemQueue = ConcurrentLinkedQueue<WriteModel<MongoSystemRecord>>()
        private val systemServerSettingsQueue = ConcurrentLinkedQueue<WriteModel<MongoSystemServerSettingsRecord>>()
        private val systemChannelSettingsQueue = ConcurrentLinkedQueue<WriteModel<MongoSystemChannelSettingsRecord>>()
        private val userQueue = ConcurrentLinkedQueue<WriteModel<MongoUserRecord>>()
        private val proxiedMessageQueue = ConcurrentLinkedQueue<WriteModel<MongoProxiedMessageRecord>>()
        private val systemSwitchQueue = ConcurrentLinkedQueue<WriteModel<MongoSystemSwitchRecord>>()
        private val memberProxiesQueue = ConcurrentLinkedQueue<WriteModel<MongoMemberProxyTagRecord>>()
        private val witness = HashSet<Any?>()

        override suspend fun getDatabaseName(): String {
            return proxy.getDatabaseName() + " (Bulk Inserter)"
        }

        override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
            if (witness.add(serverSettings))
                serverSettingsQueue += (if (serverSettings is MongoServerSettingsRecord) serverSettings else MongoServerSettingsRecord(serverSettings)).replace()
        }

        override suspend fun updateChannel(channel: ChannelSettingsRecord) {
            if (witness.add(channel)) channelSettingsQueue += channel.toMongo().replace()
        }

        override suspend fun updateMember(member: MemberRecord) {
            if (witness.add(member)) memberQueue += member.toMongo().replace()
        }

        override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
            if (witness.add(serverSettings)) memberServerSettingsQueue += serverSettings.toMongo().replace()
        }

        override suspend fun updateSystem(system: SystemRecord) {
            if (witness.add(system))
                systemQueue += system.toMongo().replace()
        }

        override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
            if (witness.add(serverSettings)) systemServerSettingsQueue += serverSettings.toMongo().replace()
        }

        override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
            if (witness.add(channelSettings)) {
                systemChannelSettingsQueue += channelSettings.toMongo().replace()
            }
        }

        override suspend fun updateUser(user: UserRecord) {
            if (witness.add(user)) userQueue += user.toMongo().replace()
        }

        override suspend fun updateMessage(message: ProxiedMessageRecord) {
            if (witness.add(message)) proxiedMessageQueue += message.toMongo().replace()
        }

        override suspend fun createServerSettings(serverSettings: ServerSettingsRecord) {
            if (witness.add(serverSettings)) serverSettingsQueue += serverSettings.toMongo().create()
        }

        override suspend fun createChannel(channel: ChannelSettingsRecord) {
            if (witness.add(channel)) channelSettingsQueue += channel.toMongo().create()
        }

        override suspend fun createMember(member: MemberRecord) {
            if (witness.add(member)) memberQueue += member.toMongo().create()
        }

        override suspend fun createMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
            if (witness.add(serverSettings)) memberServerSettingsQueue += serverSettings.toMongo().create()
        }

        override suspend fun createSystem(system: SystemRecord) {
            systemQueue += system.toMongo().create()
        }

        override suspend fun createSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
            if (witness.add(serverSettings)) systemServerSettingsQueue += serverSettings.toMongo().create()
        }

        override suspend fun createSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
            if (witness.add(channelSettings)) systemChannelSettingsQueue += channelSettings.toMongo().create()
        }

        override suspend fun createUser(user: UserRecord) {
            if (witness.add(user)) userQueue += user.toMongo().create()
        }

        override suspend fun createMessage(message: ProxiedMessageRecord) {
            if (witness.add(message)) proxiedMessageQueue += message.toMongo().create()
        }

        override suspend fun updateSwitch(switch: SystemSwitchRecord) {
            if (witness.add(switch)) systemSwitchQueue += switch.toMongo().replace()
        }

        override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
            if (witness.add(record)) memberProxiesQueue += record.toMongo().create()
            return true
        }

        override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord {
            val record = SystemSwitchRecord(systemId, "", memberId, timestamp)
            systemSwitchQueue += record.toMongo().create()
            return record
        }

        override suspend fun createSwitch(switch: SystemSwitchRecord) {
            if (witness.add(switch)) systemSwitchQueue += switch.toMongo().create()
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
            memberQueue += member.toMongo().delete()
            return true
        }

        override suspend fun dropSwitch(switch: SystemSwitchRecord) {
            systemSwitchQueue += switch.toMongo().delete()
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
            systemQueue += system.toMongo().delete()
            userQueue += DeleteManyModel(filter)
            return true
        }

        override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
            memberProxiesQueue += proxyTag.toMongo().delete()
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