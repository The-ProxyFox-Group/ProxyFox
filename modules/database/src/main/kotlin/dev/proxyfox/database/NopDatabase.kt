package dev.proxyfox.database

import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.ServerSettingsRecord
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.misc.UserRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord

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

    override suspend fun getProxiesByHost(userId: String): List<MemberProxyTagRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxiesByHostAndMember(userId: String, memberId: String): List<MemberProxyTagRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberFromMessage(userId: String, message: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxyTagFromMessage(userId: String, message: String): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberServerSettingsByHost(
        serverId: String,
        userId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberServerSettingsById(
        serverId: String,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByHost(serverId: String, userId: String): SystemServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsById(serverId: String, systemId: String): SystemServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettings(serverId: String): ServerSettingsRecord {
        TODO("Not yet implemented")
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(userId: String): SystemRecord {
        TODO("Not yet implemented")
    }

    override suspend fun removeSystem(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun removeMember(systemId: String, memberId: String): Boolean {
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

    override suspend fun updateUser(user: UserRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTrustLevel(userId: String, trustee: String, level: TrustLevel): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getTrustLevel(userId: String, trustee: String): TrustLevel {
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