package dev.proxyfox.database

import dev.kord.common.entity.Snowflake
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.ServerSettingsRecord
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.misc.UserRecord
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord

class NopDatabase : Database() {
    override suspend fun setup() = this

    override suspend fun getUser(userId: String): UserRecord? = null

    override suspend fun getSystemByHost(userId: String): SystemRecord? = null

    override suspend fun getSystemById(systemId: String): SystemRecord? = null

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? = null

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? = null

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? = null

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? = null

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? = null

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?>? = null

    override suspend fun getProxiesByHost(userId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getProxiesByHostAndMember(userId: String, memberId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? = null

    override suspend fun getMemberFromMessage(userId: String, message: String): MemberRecord? = null

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? = null

    override suspend fun getMemberServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun getMemberServerSettingsById(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? = null

    override suspend fun getServerSettingsById(serverId: String, systemId: String): SystemServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettings(serverId: String): ServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {}

    override suspend fun getChannelSettings(channelId: String, systemId: String): SystemChannelSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: String): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun removeSystem(userId: String): Boolean = false

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord? = null

    override suspend fun removeMember(systemId: String, memberId: String): Boolean = false

    override suspend fun updateMember(member: MemberRecord) {}

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {}

    override suspend fun updateSystem(system: SystemRecord) {}

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {}

    override suspend fun updateUser(user: UserRecord) {}

    override suspend fun createMessage(
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        memberId: String,
        systemId: String
    ) {
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? = null

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {}

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel): Boolean = false

    override suspend fun getTrustLevel(userId: String, trustee: String): TrustLevel = TrustLevel.NONE

    override suspend fun getTotalSystems(): Int = 0

    override suspend fun getTotalMembersByHost(userId: String): Int? = null

    override suspend fun getTotalMembersById(systemId: String): Int? = null

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? = null

    override suspend fun getMemberByHostAndName(userId: String, memberName: String): MemberRecord? = null

    override suspend fun export(other: Database) {}

    override fun close() {}
}