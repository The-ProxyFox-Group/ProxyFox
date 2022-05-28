package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.ServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord

// Created 2022-26-05T19:47:37

/**
 * JSON flat file database. Warning, all mutations *will* immediately take effect and are inherently unsafe.
 *
 * ## Expected File Structure
 * ```json5
 * {
 *   "discordId": {
 *     // Will not be changed unless a backwards-incompatible change is required.
 *     "version": 1,
 *     // System ID (not to be confused with the Discord ID)
 *     "id": "string.of(systemId)",
 *     "name": "string",
 *     "description": "string",
 *     "tag": "string",
 *     // Map to `avatarUrl`
 *     "avatar_url": "url",
 *     "timezone": "string",
 *     "members": [
 *       {
 *         // Member ID
 *         "id": "string",
 *         "name": "string",
 *         // Map to `displayName`
 *         "display_name": "string",
 *         "description": "string",
 *         "birthday": "string",
 *         "pronouns": "string",
 *         // Map to integer or -1
 *         "color": "#hex",
 *         // Map to `avatarUrl`
 *         "avatar_url": "url",
 *         // Fairly complex indexing required
 *         "proxy_tags": [
 *           {
 *             "prefix": "string",
 *             "suffix": "string"
 *           },
 *           ...
 *         ],
 *         // Map to `keepProxy`
 *         "keep_proxy": false,
 *         // Map to `messageCount`
 *         "message_count": 0,
 *         "created": "ISO 8601",
 *
 *         // == Map the following to guild settings ==
 *         "server_avatar": {
 *           "guildId": "url"
 *         },
 *         "server_nick": {
 *           "guildId": "string"
 *         }
 *         "server_proxy": {
 *           "guildId": false
 *         }
 *       },
 *       ...
 *     ],
 *     // Map to `autoProxy`
 *     "auto": "memberId"
 *     // Map to `autoProxyEnabled` via converting to AutoProxyMode.
 *     "auto_bool", false,
 *     "switches": [
 *       {
 *         "timestamp": "ISO 8601",
 *         "members": [ "memberId", ... ],
 *         "id": "switchId"
 *       },
 *       ...
 *     ],
 *
 *     // == Map the following to guild settings ==
 *     "server_proxy": {
 *       "guildId": false
 *     }
 *   }
 * }
 * ```
 *
 * @author Ampflower
 * @since ${version}
 **/
class JsonDatabase : Database() {
    // Entries that must be saved
    private val systemsByDiscordId = HashMap<ULong, SystemRecord>()

    // TODO: Map<String, Member>?
    private val membersByDiscordId = HashMap<ULong, List<MemberRecord>>()

    @Transient
    private val systemsBySystemId = HashMap<String, SystemRecord>()

    @Transient
    private val membersBySystemId = HashMap<String, List<MemberRecord>>()

    override suspend fun getSystemByHost(userId: Snowflake) = systemsByDiscordId[userId.value]

    override suspend fun getSystemById(systemId: String) = systemsBySystemId[systemId]

    override suspend fun getMembersByHost(userId: Snowflake) = membersByDiscordId[userId.value]

    override suspend fun getMembersBySystem(systemId: String) = membersBySystemId[systemId]

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

    override suspend fun addUserToSystem(discordId: Snowflake, systemId: String) {
        throw UnsupportedOperationException("JSON flat file doesn't support multi-user.")
    }

    override suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String) {
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