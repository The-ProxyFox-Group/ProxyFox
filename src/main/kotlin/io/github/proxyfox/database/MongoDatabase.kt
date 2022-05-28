package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.ServerSettingsRecord
import io.github.proxyfox.database.records.misc.TrustLevel
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord

// Created 2022-26-05T22:43:40

/**
 * @author KJP12
 * @since ${version}
 **/
class MongoDatabase : Database() {
    override suspend fun getSystemByHost(userId: Snowflake): SystemRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembersByHost(userId: Snowflake): List<MemberRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getMembersBySystem(systemId: String): List<MemberRecord>? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByHost(discordId: Snowflake, memberId: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberById(systemId: String, memberId: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingMemberByHost(discordId: Snowflake): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingMemberByTags(discordId: Snowflake, message: String): Pair<MemberRecord, String>? {
        TODO("Not yet implemented")
    }

    override suspend fun getProxyTagFromMessage(discordId: Snowflake, message: String): MemberProxyTagRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake, memberId: String): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): SystemServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(discordId: Snowflake): SystemRecord {
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

    override suspend fun addUserToSystem(discordId: Snowflake, systemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTrustLevel(userId: Snowflake, trustee: Snowflake, level: TrustLevel) {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalSystems(): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalMembersByHost(discordId: Snowflake): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalMembersById(systemId: String): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberByHostAndName(discordId: Snowflake, memberName: String): MemberRecord? {
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