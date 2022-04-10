package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake

class NopDatabase : Database {
    override fun getSystemByHost(userId: Snowflake): SystemRecord? = null

    override fun getSystemById(systemId: String): SystemRecord? = null

    override fun getMembersByHost(userId: Snowflake): List<MemberRecord>? = null

    override fun getMembersBySystem(systemId: String): List<MemberRecord>? = null

    override fun getMemberByHost(discordId: Snowflake, memberId: String): MemberRecord? = null

    override fun getMemberById(systemId: String, memberId: String): MemberRecord? = null

    override fun getFrontingMemberByHost(discordId: Snowflake): MemberRecord? = null

    override fun getFrontingMemberByTags(discordId: Snowflake, message: String): MemberRecord? = null

    override fun getProxyTagFromMessage(discordId: Snowflake, message: String): MemberProxyTagRecord? = null

    override fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): MemberServerSettingsRecord? = null

    override fun getServerSettingsByHost(
        serverId: Snowflake,
        discordId: Snowflake,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override fun getServerSettingsByMember(
        serverId: Snowflake,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? = null

    override fun allocateSystem(discordId: Snowflake): SystemRecord? = null

    override fun allocateMember(systemId: String, name: String): MemberRecord? = null

    override fun updateMember(member: MemberRecord) {
        return
    }

    override fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        return
    }

    override fun updateSystem(system: SystemRecord) {
        return
    }

    override fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        return
    }

    override fun addUserToSystem(discordId: Snowflake, systemId: String) {
        return
    }

    override fun removeUserFromSystem(discordId: Snowflake, systemId: String) {
        return
    }

    override fun getTotalSystems(): Int? = null

    override fun getTotalMembersByHost(discordId: Snowflake): Int? = null

    override fun getTotalMembersById(systemId: String): Int? = null

}