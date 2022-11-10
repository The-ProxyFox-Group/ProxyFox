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
import java.time.Instant
import kotlin.time.Duration

class NopDatabase : Database() {
    override suspend fun setup() = this
    override suspend fun ping(): Duration {
        return Duration.ZERO
    }

    override suspend fun getDatabaseName() = "No Operation (NOP)"

    override suspend fun fetchUser(userId: ULong): UserRecord? = null

    override suspend fun getOrCreateUser(userId: ULong): UserRecord = fail("Cannot store user for $userId")

    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? = null

    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord>? = null

    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? = null

    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord>? = null

    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? = null

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        fail("No such system $systemId (got server $serverId)")
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord {
        fail("Cannot store server settings for $serverId")
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {}

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        fail("No such system $systemId (got channel $channelId)")
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        fail("Cannot store channel settings for $serverId -> $channelId.")
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        fail("Cannot store system for $userId -> $id.")
    }

    override suspend fun dropSystem(userId: ULong): Boolean = false

    override suspend fun dropMember(systemId: String, memberId: String): Boolean = false

    override suspend fun updateMember(member: MemberRecord) {}

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {}

    override suspend fun updateSystem(system: SystemRecord) {}

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {}
    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {}

    override suspend fun updateUser(user: UserRecord) {}

    override suspend fun createMessage(
        userId: Snowflake,
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        channelBehavior: ChannelBehavior,
        memberId: String,
        systemId: String,
        memberName: String
    ) {}

    override suspend fun updateMessage(message: ProxiedMessageRecord) {}

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? = null

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? = null

    override suspend fun getOrCreateTokenFromSystem(systemId: String): TokenRecord = fail("Cannot store token for $systemId.")

    override suspend fun updateToken(token: TokenRecord) = fail("Cannot store token for ${token.systemId}.")

    override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean = false

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord? = null
    override suspend fun dropSwitch(switch: SystemSwitchRecord) {}
    override suspend fun updateSwitch(switch: SystemSwitchRecord) {}

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord>? = null

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {}

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean = false

    override suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel = TrustLevel.NONE

    override suspend fun fetchTotalSystems(): Int = 0

    override suspend fun fetchTotalMembersFromSystem(systemId: String): Int? = null

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String, caseSensitive: Boolean): MemberRecord? = null

    override suspend fun export(other: Database) {}

    @Deprecated("Not for regular use.", level = DeprecationLevel.ERROR)
    override suspend fun drop() {
    }

    override suspend fun firstFreeSystemId(id: String?): String {
        // Always free, for as we can't store systems in this database.
        return "aaaaa"
    }

    override fun close() {}
}