package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.ServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord

class NopDatabase : Database() {
    override suspend fun getSystemByHost(userId: Snowflake): SystemRecord? = null

    override suspend fun getSystemById(systemId: String): SystemRecord? = null

    override suspend fun getMembersByHost(userId: Snowflake): List<MemberRecord>? = null

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? = null

    override suspend fun getMemberByHost(discordId: Snowflake, memberId: String): MemberRecord? = null

    override suspend fun getMemberByHostAndName(discordId: Snowflake, memberName: String): MemberRecord? = null

    override suspend fun export(other: Database) {}

    override suspend fun import(memberProxyTagRecord: MemberProxyTagRecord) {}

    override suspend fun import(memberRecord: MemberRecord) {}

    override suspend fun import(memberServerSettingsRecord: MemberServerSettingsRecord) {}

    override suspend fun import(serverSettingsRecord: ServerSettingsRecord) {}

    override suspend fun import(system: SystemRecord) {}

    override suspend fun import(systemServerSettingsRecord: SystemServerSettingsRecord) {}

    override suspend fun import(systemSwitchRecord: SystemSwitchRecord) {}

    override fun close() {}

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? = null

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? = null

    override suspend fun getFrontingMemberByHost(discordId: Snowflake): MemberRecord? = null

    override suspend fun getFrontingMemberByTags(discordId: Snowflake, message: String): Pair<MemberRecord, String>? = null

    override suspend fun getProxyTagFromMessage(discordId: Snowflake, message: String): MemberProxyTagRecord? = null

    override suspend fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): MemberServerSettingsRecord? = null

    override suspend fun getServerSettingsByHost(
        serverId: Snowflake,
        discordId: Snowflake,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): SystemServerSettingsRecord? = null

    override suspend fun getServerSettingsByMember(
        serverId: Snowflake,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override suspend fun allocateSystem(discordId: Snowflake) = SystemRecord("aaaaa")

    override suspend fun allocateMember(systemId: String, name: String) = MemberRecord("aaaaa", systemId, name = name)

    override suspend fun updateMember(member: MemberRecord) {
        return
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        return
    }

    override suspend fun updateSystem(system: SystemRecord) {
        return
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        return
    }

    override suspend fun addUserToSystem(discordId: Snowflake, systemId: String) {
        return
    }

    override suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String) {
        return
    }

    override suspend fun getTotalSystems(): Int? = null

    override suspend fun getTotalMembersByHost(discordId: Snowflake): Int? = null

    override suspend fun getTotalMembersById(systemId: String): Int? = null

}