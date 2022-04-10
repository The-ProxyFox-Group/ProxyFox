package io.github.proxyfox.database

class NopDatabase : Database {
    override fun getSystemByHost(userId: ULong): SystemRecord? {
        return null
    }

    override fun getSystemById(systemId: String): SystemRecord? {
        return null
    }

    override fun getMembersByHost(userId: ULong): List<MemberRecord>? {
        return null
    }

    override fun getMembersBySystem(systemId: String): List<MemberRecord>? {
        return null
    }

    override fun getMemberByHost(discordId: ULong, memberId: String): MemberRecord? {
        return null
    }

    override fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        return null
    }

    override fun getFrontingMemberByHost(discordId: ULong): MemberRecord? {
        return null
    }

    override fun getFrontingMemberByTags(discordId: ULong, message: String): MemberRecord? {
        return null
    }

    override fun getFrontingServerSettingsByHost(serverId: ULong, discordId: ULong): MemberServerSettingsRecord? {
        return null
    }

    override fun getServerSettingsByHost(
        serverId: ULong,
        discordId: ULong,
        memberId: String
    ): MemberServerSettingsRecord? {
        return null
    }

    override fun getServerSettingsByMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        return null
    }

    override fun allocateSystem(discordId: ULong): SystemRecord? {
        return null
    }

    override fun allocateMember(systemId: String, name: String): MemberRecord? {
        return null
    }

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

    override fun addUserToSystem(discordId: ULong, systemId: String) {
        return
    }

    override fun removeUserFromSystem(discordId: ULong, systemId: String) {
        return
    }

    override fun getTotalSystems(): Int? {
        return null
    }

    override fun getTotalMembersByHost(discordId: ULong): Int? {
        return null
    }

    override fun getTotalMembersById(systemId: String): Int? {
        return null
    }

}