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
import java.time.OffsetDateTime

class NopDatabase : Database() {
    override suspend fun setup() = this

    override suspend fun getUser(userId: ULong): UserRecord? = null

    override suspend fun getSystemById(systemId: String): SystemRecord? = null

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? = null

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? = null

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getMemberServerSettingsById(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun getServerSettingsById(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettings(serverId: ULong): ServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {}

    override suspend fun getChannelSettings(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: ULong, id: String?): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun removeSystem(userId: ULong): Boolean = false

    override suspend fun allocateMember(systemId: String, name: String, id: String?): MemberRecord? = null

    override suspend fun removeMember(systemId: String, memberId: String): Boolean = false

    override suspend fun updateMember(member: MemberRecord) {}

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {}

    override suspend fun updateSystem(system: SystemRecord) {}

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {}
    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {}

    override suspend fun updateUser(user: UserRecord) {}

    override suspend fun createMessage(oldMessageId: Snowflake, newMessageId: Snowflake, channelBehavior: ChannelBehavior, memberId: String, systemId: String) {}

    override suspend fun updateMessage(message: ProxiedMessageRecord) {}

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? = null

    override suspend fun fetchLatestMessage(
        systemId: String,
        channelId: Snowflake
    ): ProxiedMessageRecord? = null

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? = null

    override suspend fun listProxyTags(systemId: String, memberId: String): List<MemberProxyTagRecord>? = null

    override suspend fun allocateSwitch(systemId: String, memberId: List<String>, timestamp: OffsetDateTime?): SystemSwitchRecord? = null
    override suspend fun removeSwitch(switch: SystemSwitchRecord) {}

    override suspend fun getLatestSwitch(systemId: String): SystemSwitchRecord? = null
    override suspend fun getSwitchesById(systemId: String): List<SystemSwitchRecord>? = null

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {}

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean = false

    override suspend fun getTrustLevel(systemId: String, trustee: ULong): TrustLevel = TrustLevel.NONE

    override suspend fun getTotalSystems(): Int = 0

    override suspend fun getTotalMembersById(systemId: String): Int? = null

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? = null

    override suspend fun export(other: Database) {}

    override fun close() {}
}