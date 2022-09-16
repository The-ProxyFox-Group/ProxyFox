/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
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

// Created 2022-09-04T14:06:39

/**
 * The generic database interface for ProxyFox.
 *
 * @author KJP12
 **/
// Suppression since unused warnings aren't useful for an API.
@Suppress("unused")
abstract class Database : AutoCloseable {
    abstract suspend fun setup(): Database

    abstract suspend fun getUser(userId: ULong): UserRecord?
    suspend inline fun getUser(user: UserBehavior?) = user?.run { getUser(id.value) }

    // === Systems ===

    /**
     * Gets a [system][SystemRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The system tied to the Discord user.
     * */
    open suspend fun getSystemByHost(userId: ULong) = getUser(userId)?.system?.let { getSystemById(it) }

    suspend inline fun getSystemByHost(user: UserBehavior?) = user?.run { getSystemByHost(id.value) }

    /**
     * Gets a [system][SystemRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return The system as registered by ID.
     * */
    abstract suspend fun getSystemById(systemId: String): SystemRecord?

    // === Members ===
    /**
     * Gets a list of [members][MemberRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return A list of members registered to the system tied to the Discord user.
     * */
    open suspend fun getMembersByHost(userId: ULong) = getUser(userId)?.system?.let { getMembersBySystem(it) }

    suspend inline fun getMembersByHost(user: UserBehavior?) = user?.run { getMembersByHost(id.value) }

    /**
     * Gets a list of [members][MemberRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return A list of members registered to the system.
     * */
    abstract suspend fun getMembersBySystem(systemId: String): List<MemberRecord>?

    /**
     * Gets the [member][MemberRecord] by both Discord & member IDs.
     *
     * @param userId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member of the system tied to the Discord user.
     * */
    open suspend fun getMemberByHost(userId: ULong, memberId: String) = getUser(userId)?.system?.let { getMemberById(it, memberId) }

    suspend inline fun getMemberByHost(user: UserBehavior?, memberId: String) = user?.run { getMemberByHost(id.value, memberId) }

    /**
     * Gets the [member][MemberRecord] by both system & member IDs.
     *
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system.
     * @return The member of the system.
     * */
    abstract suspend fun getMemberById(systemId: String, memberId: String): MemberRecord?

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    open suspend fun getFrontingMembersByHost(userId: ULong) = getUser(userId)?.system?.let { getFrontingMembersById(it) }

    suspend inline fun getFrontingMembersByHost(user: UserBehavior?) = user?.run { getFrontingMembersByHost(id.value) }

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID.
     *
     * @param systemId The ID of the system.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    open suspend fun getFrontingMembersById(systemId: String): List<MemberRecord>? {
        return getLatestSwitch(systemId)?.memberIds?.mapNotNull { getMemberById(systemId, it) }
    }

    open suspend fun getProxiesByHost(userId: ULong) = getUser(userId)?.system?.let { getProxiesById(it) }

    suspend inline fun getProxiesByHost(user: UserBehavior) = getProxiesByHost(user.id.value)

    abstract suspend fun getProxiesById(systemId: String): List<MemberProxyTagRecord>?

    suspend inline fun getProxiesByHostAndMember(user: UserBehavior, memberId: String) = getProxiesByHostAndMember(user.id.value, memberId)

    open suspend fun getProxiesByHostAndMember(userId: ULong, memberId: String) = getUser(userId)?.system?.let { getProxiesByIdAndMember(it, memberId) }

    abstract suspend fun getProxiesByIdAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>?

    /**
     * Gets the [proxy][MemberProxyTagRecord] by Discord ID and proxy tags.
     *
     * @param userId The ID of the Discord user.
     * @param message The message to check proxy tags against.
     * @return The ProxyTag associated with the message
     * */
    open suspend fun getMemberFromMessage(userId: ULong, message: String) = getProxyTagFromMessage(userId, message)?.memberId?.let { getMemberByHost(userId, it) }

    suspend inline fun getMemberFromMessage(user: UserBehavior?, message: String) = user?.run { getMemberFromMessage(id.value, message) }

    open suspend fun getProxyTagFromMessage(userId: ULong, message: String) = getProxiesByHost(userId)?.find { it.test(message) }

    suspend inline fun getProxyTagFromMessage(user: UserBehavior?, message: String) = user?.run { getProxyTagFromMessage(id.value, message) }

    // === Server Settings ===
    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, Discord & member IDs.
     *
     * @param serverId The ID of the server.
     * @param userId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    open suspend fun getMemberServerSettingsByHost(
        serverId: ULong,
        userId: ULong,
        memberId: String
    ) = getUser(userId)?.system?.let { getMemberServerSettingsById(serverId, it, memberId) }

    suspend inline fun getMemberServerSettingsByHost(
        server: GuildBehavior,
        user: UserBehavior,
        memberId: String
    ) = getMemberServerSettingsByHost(server.id.value, user.id.value, memberId)

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, system & member IDs.
     *
     * @param serverId The ID of the server.
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    abstract suspend fun getMemberServerSettingsById(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord?

    suspend inline fun getMemberServerSettingsById(
        server: GuildBehavior?,
        systemId: String,
        memberId: String
    ) = server?.run { getMemberServerSettingsById(id.value, systemId, memberId) }

    /**
     * Gets the [system's server settings][SystemServerSettingsRecord] by server & Discord IDs.
     *
     * @param serverId The ID of the server.
     * @param userId The ID of the Discord user.
     * @return The system's settings for the server.
     * */
    open suspend fun getServerSettingsByHost(serverId: ULong, userId: ULong) =
        getUser(userId)?.system?.let { getServerSettingsById(serverId, it) }

    suspend inline fun getServerSettingsByHost(server: GuildBehavior, user: UserBehavior) = getServerSettingsByHost(server.id.value, user.id.value)

    abstract suspend fun getServerSettingsById(serverId: ULong, systemId: String): SystemServerSettingsRecord

    suspend inline fun getServerSettingsById(server: GuildBehavior, systemId: String) = getServerSettingsById(server.id.value, systemId)

    suspend inline fun getServerSettings(server: GuildBehavior) = getServerSettings(server.id.value)

    abstract suspend fun getServerSettings(serverId: ULong): ServerSettingsRecord

    abstract suspend fun updateServerSettings(serverSettings: ServerSettingsRecord)

    suspend inline fun getChannelSettings(channel: ChannelBehavior, systemId: String) = getChannelSettings(channel.id.value, systemId)

    abstract suspend fun getChannelSettings(channelId: ULong, systemId: String): SystemChannelSettingsRecord

    abstract suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord
    abstract suspend fun updateChannel(channel: ChannelSettingsRecord)

    // === Management ===
    /**
     * Allocates or reuses a system ID in the database.
     *
     * @param userId The ID of the Discord user.
     * @return A maybe newly created system. Never null.
     * */
    abstract suspend fun allocateSystem(userId: ULong, id: String? = null): SystemRecord

    suspend inline fun allocateSystem(user: UserBehavior, id: String? = null) = allocateSystem(user.id.value, id)

    abstract suspend fun removeSystem(userId: ULong): Boolean

    suspend inline fun removeSystem(user: UserBehavior) = removeSystem(user.id.value)

    /**
     * Allocates a member ID in the database.
     *
     * @param systemId The ID of the system.
     * @param name The name of the new member.
     * @return A newly created member. null if system doesn't exist.
     * */
    abstract suspend fun allocateMember(systemId: String, name: String, id: String? = null): MemberRecord?
    abstract suspend fun removeMember(systemId: String, memberId: String): Boolean

    // TODO: This ideally needs a much better system for updating since this really isn't ideal as is.
    //  This applies to the following 4 methods below.
    abstract suspend fun updateMember(member: MemberRecord)
    abstract suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord)
    abstract suspend fun updateSystem(system: SystemRecord)
    abstract suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord)
    abstract suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord)
    abstract suspend fun updateUser(user: UserRecord)

    abstract suspend fun createMessage(
            oldMessageId: Snowflake,
            newMessageId: Snowflake,
            channelBehavior: ChannelBehavior,
            memberId: String,
            systemId: String
    )
    abstract suspend fun updateMessage(message: ProxiedMessageRecord)
    abstract suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord?
    abstract suspend fun fetchLatestMessage(systemId: String, channelId: Snowflake): ProxiedMessageRecord?

    /**
     * Allocates a proxy tag
     * @param systemId The system ID to assign it to
     * @param memberId The member to assign it to
     * @param prefix The prefix of the proxy
     * @param suffix the suffix of the proxy
     * @return The newly created proxy tag, if one with the same prefix and suffix exists already, return null
     * */
    abstract suspend fun allocateProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord?

    /**
     * Lists all proxy tags registered for the member.
     * @param systemId The system ID to assign it to
     * @param memberId The member to assign it to
     * @return All proxy tags registered for the member, else null if the member or system doesn't exist.
     * */
    abstract suspend fun listProxyTags(
        systemId: String,
        memberId: String
    ): List<MemberProxyTagRecord>?

    /**
     * Allocates a switch
     *
     * @param systemId The system ID to assign it to
     * @param memberId The member IDs for the switch
     * @param timestamp The timestamp of the switch. May be null for now.
     * @return A switch if a system exists, null otherwise.
     * */
    abstract suspend fun allocateSwitch(
        systemId: String,
        memberId: List<String>,
        timestamp: OffsetDateTime? = null
    ): SystemSwitchRecord?

    /**
     *
     * */
    abstract suspend fun removeSwitch(switch: SystemSwitchRecord)

    /**
     *
     * */
    abstract suspend fun updateSwitch(switch: SystemSwitchRecord)

    /**
     *
     * */
    suspend fun getLatestSwitch(systemId: String): SystemSwitchRecord? =
        getSwitchesById(systemId)?.maxByOrNull {
            it.timestamp
        }

    /**
     *
     * */
    suspend fun getSecondLatestSwitch(systemId: String): SystemSwitchRecord? {
        val switches = getSortedSwitchesById(systemId)
            ?: return null

        if (switches.size < 2) return null

        return switches[1]
    }

    /**
     *
     * */
    suspend fun getSortedSwitchesById(
        systemId: String
    ): List<SystemSwitchRecord>? = getSwitchesById(systemId)?.sortedByDescending { it.timestamp }

    /**
     * Get switches by user ID
     *
     * @param userId The user ID to get all switches by.
     * @return All switches registered for the system.
     * */
    open suspend fun getSwitchesByHost(
        userId: ULong
    ) = getUser(userId)?.system?.let { getSwitchesById(it) }

    suspend inline fun getSwitchesByHost(
        user: UserBehavior?
    ) = user?.run { getSwitchesByHost(id.value) }

    /**
     * Get switches by system ID
     *
     * @param systemId The system ID to get all switches by.
     * @return All switches registered for the system.
     * */
    abstract suspend fun getSwitchesById(
        systemId: String
    ): List<SystemSwitchRecord>?

    /**
     * Removes a proxy tag
     * @param proxyTag The proxy tag to remove
     * */
    abstract suspend fun removeProxyTag(proxyTag: MemberProxyTagRecord)

    /**
     * Updates the trust level for the trustee
     * @param trustee The owner of the system
     * @param systemId The person being trusted
     * @param level the level of trust granted
     * */
    abstract suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean

    abstract suspend fun getTrustLevel(systemId: String, trustee: ULong): TrustLevel

    /**
     * Gets the total number of systems registered
     *
     * Implementation requirements: return an int with the total systems in the database
     * */
    abstract suspend fun getTotalSystems(): Int?

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    suspend inline fun getTotalMembersByHost(user: UserBehavior?) = getUser(user)?.system?.let { getTotalMembersById(it) } ?: -1

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    abstract suspend fun getTotalMembersById(systemId: String): Int?

    /**
     * Gets a member by system ID and member name
     * */
    abstract suspend fun getMemberByIdAndName(systemId: String, memberName: String): MemberRecord?

    /**
     * Gets a member by system ID and either member ID or name.
     * */
    suspend fun findMember(systemId: String, member: String): MemberRecord? = getMemberByIdAndName(systemId, member) ?: getMemberById(systemId, member)

    /**
     * Gets a member by user snowflake and member name
     * */
    suspend inline fun getMemberByHostAndName(user: UserBehavior, memberName: String) = getUser(user)?.system?.let { getMemberByIdAndName(it, memberName) }

    // === Unsafe direct-write import & export functions ===
    abstract suspend fun export(other: Database)
}