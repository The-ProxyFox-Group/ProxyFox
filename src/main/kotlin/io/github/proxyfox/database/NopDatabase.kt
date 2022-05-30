package io.github.proxyfox.database

import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.ServerSettingsRecord
import io.github.proxyfox.database.records.misc.TrustLevel
import io.github.proxyfox.database.records.misc.UserRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord

class NopDatabase : Database() {
    override suspend fun getUser(userId: String): UserRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getSystemByHost(userId: String): SystemRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembersByHost(userId: String): List<MemberRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByHost(userId: String, memberId: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingMembersByHost(userId: String): List<MemberRecord?>? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingMembersById(systemId: String): List<MemberRecord?>? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

}