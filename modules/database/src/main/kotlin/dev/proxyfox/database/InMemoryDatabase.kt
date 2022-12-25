/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Instant
import kotlin.time.Duration

class InMemoryDatabase : Database() {
    private var users = HashMap<ULong, UserRecord>()

    private var messages = ArrayList<ProxiedMessageRecord>()

    private var servers = HashMap<ULong, ServerSettingsRecord>()
    private var channels = HashMap<ULong, HashMap<ULong, ChannelSettingsRecord>>()

    private var systems = HashMap<String, SystemRecord>()
    private var systemSwitches = HashMap<String, ArrayList<SystemSwitchRecord>>()
    private var systemTokens = HashMap<String, TokenRecord>()

    private var systemServers = HashMap<String, HashMap<ULong, SystemServerSettingsRecord>>()
    private var systemChannels = HashMap<String, HashMap<ULong, SystemChannelSettingsRecord>>()

    private var members = HashMap<String, HashMap<String, MemberRecord>>()
    private var memberProxies = HashMap<String, ArrayList<MemberProxyTagRecord>>()

    private var memberServers = HashMap<String, HashMap<ULong, MemberServerSettingsRecord>>()


    override suspend fun setup() = this
    override suspend fun ping(): Duration {
        return Duration.ZERO
    }

    override suspend fun getDatabaseName() = "In-Memory"

    override suspend fun fetchUser(userId: ULong): UserRecord? = users[userId]

    override suspend fun getOrCreateUser(userId: ULong): UserRecord {
        if (!users.containsKey(userId)) {
            users[userId] = UserRecord()
        }

        return users[userId]!!
    }

    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? = systems[systemId]

    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord>? = members[systemId]?.values?.toList()

    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? = fetchMembersFromSystem(systemId)?.find { it.id == memberId }

    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord>? {
        val members = fetchMembersFromSystem(systemId)
        return members?.mapNotNull { member -> memberProxies[member.id] }?.flatten()
    }

    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        val member = fetchMemberFromSystem(systemId, memberId)
        return memberProxies[member?.id]
    }

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = memberServers[fetchMemberFromSystem(systemId, memberId)?.id]?.get(serverId)

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        if (!systemServers.containsKey(systemId)) {
            systemServers[systemId] = HashMap()
        }

        if (!systemServers[systemId]!!.containsKey(serverId)) {
            systemServers[systemId]!![serverId] = SystemServerSettingsRecord()
        }

        return systemServers[systemId]!![serverId]!!
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord {
        if (!servers.containsKey(serverId)) {
            servers[serverId] = ServerSettingsRecord(serverId)
        }

        return servers[serverId]!!
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        servers[serverSettings.serverId] = serverSettings
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        if (!systemChannels.containsKey(systemId)) {
            systemChannels[systemId] = HashMap()
        }

        if (!systemChannels[systemId]!!.containsKey(channelId)) {
            systemChannels[systemId]!![channelId] = SystemChannelSettingsRecord()
        }

        return systemChannels[systemId]!![channelId]!!
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        getOrCreateServerSettings(serverId)
        if (!channels.containsKey(serverId)) {
            channels[serverId] = HashMap()
        }

        if (!channels[serverId]!!.containsKey(channelId)) {
            channels[serverId]!![channelId] = ChannelSettingsRecord(serverId, channelId)
        }

        return channels[serverId]!![channelId]!!
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        getOrCreateChannel(channel.serverId, channel.channelId)
        channels[channel.serverId]!![channel.channelId] = channel
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        if (users.containsKey(userId) && users[userId]!!.systemId != null && systems.containsKey(users[userId]!!.systemId)) {
            return systems[users[userId]!!.systemId]!!
        }

        if (id != null && systems.containsKey(id)) {
            return systems[id]!!
        }

        val system = SystemRecord()
        systems[system.id] = system
        systemSwitches[system.id] = ArrayList()
        systemServers[system.id] = HashMap()
        systemChannels[system.id] = HashMap()
        members[system.id] = HashMap()

        val user = getOrCreateUser(userId)
        user.systemId = system.id

        return system
    }

    override suspend fun dropSystem(userId: ULong): Boolean {
        val system = fetchSystemFromUser(userId) ?: return false
        systems.remove(system.id)
        return true
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        val member = members[systemId]?.remove(memberId)
        return member != null
    }

    override suspend fun updateMember(member: MemberRecord) {
        members[member.systemId]?.set(member.id, member)
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        memberServers[serverSettings.memberId]?.set(serverSettings.serverId, serverSettings)
    }

    override suspend fun updateSystem(system: SystemRecord) {
        systems[system.id] = system
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        systemServers[serverSettings.systemId]?.set(serverSettings.serverId, serverSettings)
    }
    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        systemChannels[channelSettings.systemId]?.set(channelSettings.channelId, channelSettings)
    }

    override suspend fun updateUser(user: UserRecord) {
        users[user.id] = user
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
        message.userId = userId.value
        message.oldMessageId = oldMessageId.value
        message.newMessageId = newMessageId.value
        message.channelId = channelBehavior.id.value
        message.memberId = memberId
        message.systemId = systemId
        message.memberName = memberName
        messages.add(message)
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        val idx = messages.indexOfFirst { it.oldMessageId == message.oldMessageId }
        messages[idx] = message
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? {
        return messages.find { it.oldMessageId == messageId.value || it.newMessageId == messageId.value }
    }

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? {
        return messages.findLast { it.systemId == systemId && it.channelId == channelId.value }
    }

    override suspend fun getOrCreateTokenFromSystem(systemId: String): TokenRecord {
        if (!systemTokens.containsKey(systemId)) systemTokens[systemId] = TokenRecord(generateToken(), systemId)
        return systemTokens[systemId]!!
    }

    override suspend fun updateToken(token: TokenRecord) {
        systemTokens[token.systemId] = token
    }

    override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
        memberProxies[record.memberId]?.add(record)
        return true
    }

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord? {
        val switches = fetchSwitchesFromSystem(systemId)
        if (switches == null) {
            systemSwitches[systemId] = ArrayList()
        }
        val id = ((switches!!.maxOfOrNull { it.id.fromPkString() } ?: 0) + 1).toPkString()
        val switch = SystemSwitchRecord(systemId, id, memberId, timestamp)
        systemSwitches[systemId]!!.add(switch)
        return switch
    }
    override suspend fun dropSwitch(switch: SystemSwitchRecord) {
        systemSwitches[switch.systemId]?.remove(switch)
    }
    override suspend fun updateSwitch(switch: SystemSwitchRecord) {
        systemSwitches[switch.systemId]?.add(switch)
    }

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord>? = systemSwitches[systemId]

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
        memberProxies[proxyTag.memberId]?.remove(proxyTag)
    }

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
        systems[systemId]?.trust?.set(trustee, level)
        return true
    }

    override suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel {
        return systems[systemId]?.trust?.get(trustee) ?: TrustLevel.NONE
    }

    override suspend fun fetchTotalSystems(): Int = systems.size

    override suspend fun fetchTotalMembersFromSystem(systemId: String): Int? = members[systemId]?.size

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String, caseSensitive: Boolean): MemberRecord? {
        return members[systemId]?.values?.find { if (caseSensitive) it.name == memberName else it.name.lowercase() == memberName.lowercase() }
    }

    override suspend fun export(other: Database) {
        TODO("Not yet implemented")
    }

    @Deprecated("Not for regular use.", level = DeprecationLevel.ERROR)
    override suspend fun drop() {
        TODO("Not yet implemented")
    }

    override suspend fun firstFreeSystemId(id: String?): String {
        return "aaaaa" // my mental state
    }

    override fun close() {}
}