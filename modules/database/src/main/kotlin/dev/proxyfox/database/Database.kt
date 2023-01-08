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
import dev.proxyfox.database.records.DatabaseException
import dev.proxyfox.database.records.group.GroupRecord
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Instant
import org.jetbrains.annotations.TestOnly
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.time.Duration

// Created 2022-09-04T14:06:39

/**
 * The generic database interface for ProxyFox.
 *
 * @author Ampflower
 **/
// Suppression since unused warnings aren't useful for an API.
/*
* TODO: Move methods that require an id for an object in the database to use that object.
*  ex: systemId: String, memberName: String -> system: SystemRecord, memberName: String
* */
@Suppress("unused")
abstract class Database : AutoCloseable {
    abstract suspend fun setup(): Database
    abstract suspend fun ping(): Duration

    abstract suspend fun getDatabaseName(): String

    abstract suspend fun fetchUser(userId: ULong): UserRecord?
    suspend inline fun fetchUser(user: UserBehavior?) = user?.run { fetchUser(id.value) }

    open suspend fun getOrCreateUser(userId: ULong): UserRecord {
        return fetchUser(userId) ?: UserRecord().apply { id = userId }
    }

    suspend inline fun getOrCreateUser(user: UserBehavior?) = getOrCreateUser(user!!.id.value)

    // === Systems ===

    /**
     * Gets a [system][SystemRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The system tied to the Discord user.
     * */
    open suspend fun fetchSystemFromUser(userId: ULong) = fetchUser(userId)?.systemId?.let { fetchSystemFromId(it) }

    suspend inline fun fetchSystemFromUser(user: UserBehavior?) = user?.run { fetchSystemFromUser(id.value) }

    /**
     * Gets a [system][SystemRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return The system as registered by ID.
     * */
    abstract suspend fun fetchSystemFromId(systemId: String): SystemRecord?

    // === Members ===
    /**
     * Gets a list of [members][MemberRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return A list of members registered to the system tied to the Discord user.
     * */
    open suspend fun fetchMembersFromUser(userId: ULong) = fetchUser(userId)?.systemId?.let { fetchMembersFromSystem(it) }

    suspend inline fun fetchMembersFromUser(user: UserBehavior?) = user?.run { fetchMembersFromUser(id.value) }

    /**
     * Gets a list of [members][MemberRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return A list of members registered to the system.
     * */
    abstract suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord>?

    /**
     * Gets the [member][MemberRecord] by both Discord & member IDs.
     *
     * @param userId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member of the system tied to the Discord user.
     * */
    open suspend fun fetchMemberFromUser(userId: ULong, memberId: String) = fetchUser(userId)?.systemId?.let { fetchMemberFromSystem(it, memberId) }

    suspend inline fun fetchMemberFromUser(user: UserBehavior?, memberId: String) = user?.run { fetchMemberFromUser(id.value, memberId) }

    /**
     * Gets the [member][MemberRecord] by both system & member IDs.
     *
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system.
     * @return The member of the system.
     * */
    abstract suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord?

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    open suspend fun fetchFrontingMembersFromUser(userId: ULong) = fetchUser(userId)?.systemId?.let { fetchFrontingMembersFromSystem(it) }

    suspend inline fun fetchFrontingMembersFromUser(user: UserBehavior?) = user?.run { fetchFrontingMembersFromUser(id.value) }

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID.
     *
     * @param systemId The ID of the system.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    open suspend fun fetchFrontingMembersFromSystem(systemId: String): List<MemberRecord>? {
        return fetchLatestSwitch(systemId)?.memberIds?.mapNotNull { fetchMemberFromSystem(systemId, it) }
    }

    open suspend fun fetchProxiesFromUser(userId: ULong) = fetchUser(userId)?.systemId?.let { fetchProxiesFromSystem(it) }

    suspend inline fun fetchProxiesFromUser(user: UserBehavior) = fetchProxiesFromUser(user.id.value)

    abstract suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord>?

    suspend inline fun fetchProxiesFromUserAndMember(user: UserBehavior, memberId: String) = fetchProxiesFromUserAndMember(user.id.value, memberId)

    open suspend fun fetchProxiesFromUserAndMember(userId: ULong, memberId: String) = fetchUser(userId)?.systemId?.let { this.fetchProxiesFromSystemAndMember(it, memberId) }

    abstract suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>?

    /**
     * Gets the [proxy][MemberProxyTagRecord] by Discord ID and proxy tags.
     *
     * @param userId The ID of the Discord user.
     * @param message The message to check proxy tags against.
     * @return The ProxyTag associated with the message
     * */
    open suspend fun fetchMemberFromMessage(userId: ULong, message: String) = fetchProxyTagFromMessage(userId, message)?.memberId?.let { fetchMemberFromUser(userId, it) }

    // Not inlined due to breaking under oddly specific conditions of bulk testing with kotlin function references.
    suspend fun fetchMemberFromMessage(user: UserBehavior?, message: String) = user?.run { fetchMemberFromMessage(id.value, message) }

    open suspend fun fetchProxyTagFromMessage(userId: ULong, message: String) = fetchProxiesFromUser(userId)?.find { it.test(message) }

    // Not inlined due to breaking under oddly specific conditions of bulk testing with kotlin function references.
    suspend fun fetchProxyTagFromMessage(user: UserBehavior?, message: String) = user?.run { fetchProxyTagFromMessage(id.value, message) }

    // === Server Settings ===
    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, Discord & member IDs.
     *
     * @param serverId The ID of the server.
     * @param userId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    open suspend fun fetchMemberServerSettingsFromUserAndMember(
        serverId: ULong,
        userId: ULong,
        memberId: String
    ) = fetchUser(userId)?.systemId?.let { fetchMemberServerSettingsFromSystemAndMember(serverId, it, memberId) }

    suspend inline fun fetchMemberServerSettingsFromUserAndMember(
        server: GuildBehavior,
        user: UserBehavior,
        memberId: String
    ) = fetchMemberServerSettingsFromUserAndMember(server.id.value, user.id.value, memberId)

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, system & member IDs.
     *
     * @param serverId The ID of the server.
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    abstract suspend fun fetchMemberServerSettingsFromSystemAndMember(
        serverId: ULong,
        systemId: String,
        memberId: String
    ): MemberServerSettingsRecord?

    suspend inline fun fetchMemberServerSettingsFromSystemAndMember(
        server: GuildBehavior?,
        systemId: String,
        memberId: String
    ) = server?.run { fetchMemberServerSettingsFromSystemAndMember(id.value, systemId, memberId) }

    /**
     * Gets the [system's server settings][SystemServerSettingsRecord] by server & Discord IDs.
     *
     * @param serverId The ID of the server.
     * @param userId The ID of the Discord user.
     * @return The system's settings for the server.
     * */
    open suspend fun getOrCreateServerSettingsFromUser(serverId: ULong, userId: ULong) =
        fetchUser(userId)?.systemId?.let { getOrCreateServerSettingsFromSystem(serverId, it) }

    suspend inline fun getOrCreateServerSettingsFromUser(server: GuildBehavior, user: UserBehavior) = getOrCreateServerSettingsFromUser(server.id.value, user.id.value)

    abstract suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord

    suspend inline fun getOrCreateServerSettingsFromSystem(server: GuildBehavior, systemId: String) = getOrCreateServerSettingsFromSystem(server.id.value, systemId)

    suspend inline fun getOrCreateServerSettings(server: GuildBehavior) = getOrCreateServerSettings(server.id.value)

    abstract suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord

    abstract suspend fun updateServerSettings(serverSettings: ServerSettingsRecord)

    open suspend fun createServerSettings(serverSettings: ServerSettingsRecord) = updateServerSettings(serverSettings)

    suspend inline fun getOrCreateChannelSettingsFromSystem(channel: ChannelBehavior, systemId: String) = getOrCreateChannelSettingsFromSystem(channel.id.value, systemId)

    abstract suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord

    abstract suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord
    abstract suspend fun updateChannel(channel: ChannelSettingsRecord)

    open suspend fun createChannel(channel: ChannelSettingsRecord) = updateChannel(channel)

    // === Management ===
    /**
     * Allocates or reuses a system ID in the database.
     *
     * @param userId The ID of the Discord user.
     * @return A maybe newly created system. Never null.
     * */
    abstract suspend fun getOrCreateSystem(userId: ULong, id: String? = null): SystemRecord

    suspend inline fun getOrCreateSystem(user: UserBehavior, id: String? = null) = getOrCreateSystem(user.id.value, id)

    open suspend fun containsSystem(systemId: String) = fetchSystemFromId(systemId) != null

    abstract suspend fun dropSystem(userId: ULong): Boolean

    suspend inline fun dropSystem(user: UserBehavior) = dropSystem(user.id.value)

    /**
     * Allocates a member ID in the database.
     *
     * @param systemId The ID of the system.
     * @param name The name of the new member.
     * @return A newly created member. null if system doesn't exist.
     * */
    open suspend fun getOrCreateMember(systemId: String, name: String, id: String? = null): MemberRecord? {
        return fetchMemberFromSystemAndName(systemId, name) ?: createMember(systemId, name, id)
    }

    open suspend fun containsMember(systemId: String, memberId: String) = fetchMemberFromSystem(systemId, memberId) != null

    abstract suspend fun dropMember(systemId: String, memberId: String): Boolean

    // TODO: This ideally needs a much better system for updating since this really isn't ideal as is.
    //  This applies to the following 4 methods below.
    abstract suspend fun updateMember(member: MemberRecord)

    open suspend fun createMember(systemId: String, name: String, id: String? = null): MemberRecord? {
        fetchSystemFromId(systemId) ?: return null
        val member = MemberRecord(
            id = firstFreeMemberId(systemId, id),
            systemId = systemId,
            name = name,
        )
        createMember(member)
        return member
    }

    open suspend fun createMember(member: MemberRecord) = updateMember(member)
    abstract suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord)

    open suspend fun createMemberServerSettings(serverSettings: MemberServerSettingsRecord) = updateMemberServerSettings(serverSettings)
    abstract suspend fun updateSystem(system: SystemRecord)

    open suspend fun createSystem(system: SystemRecord) = updateSystem(system)
    abstract suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord)

    open suspend fun createSystemServerSettings(serverSettings: SystemServerSettingsRecord) = updateSystemServerSettings(serverSettings)
    abstract suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord)

    open suspend fun createSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) = updateSystemChannelSettings(channelSettings)
    abstract suspend fun updateUser(user: UserRecord)

    open suspend fun createUser(user: UserRecord) = updateUser(user)

    abstract suspend fun createMessage(
        userId: Snowflake,
        oldMessageId: Snowflake,
        newMessageId: Snowflake,
        channelBehavior: ChannelBehavior,
        memberId: String,
        systemId: String,
        memberName: String
    )

    abstract suspend fun updateMessage(message: ProxiedMessageRecord)

    open suspend fun createMessage(message: ProxiedMessageRecord) = updateMessage(message)
    abstract suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord?
    abstract suspend fun fetchLatestMessage(systemId: String, channelId: Snowflake): ProxiedMessageRecord?

    abstract suspend fun getOrCreateTokenFromSystem(systemId: String): TokenRecord

    abstract suspend fun updateToken(token: TokenRecord)

    /**
     * Allocates a proxy tag
     * @param systemId The system ID to assign it to
     * @param memberId The member to assign it to
     * @param prefix The prefix of the proxy
     * @param suffix the suffix of the proxy
     * @return The newly created proxy tag, if one with the same prefix and suffix exists already, return null
     * */
    open suspend fun createProxyTag(
        systemId: String,
        memberId: String,
        prefix: String?,
        suffix: String?
    ): MemberProxyTagRecord? {
        if (prefix.isNullOrEmpty() && suffix.isNullOrEmpty()) return null
        fetchProxiesFromSystem(systemId)?.firstOrNull { it.isEqual(prefix, suffix) }?.let {
            return if (it.memberId == memberId) it else null
        }
        return MemberProxyTagRecord(
            memberId = memberId,
            systemId = systemId,
            prefix = prefix,
            suffix = suffix,
        ).also { createProxyTag(it) }
    }

    /**
     * Allocates a proxy tag
     * @param record The proxy tag to add.
     * @return The newly created proxy tag, if one with the same prefix and suffix exists already, return null
     * */
    abstract suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean

    /**
     * Allocates a switch
     *
     * @param systemId The system ID to assign it to
     * @param memberId The member IDs for the switch
     * @param timestamp The timestamp of the switch. May be null for now.
     * @return A switch if a system exists, null otherwise.
     * */
    abstract suspend fun createSwitch(
        systemId: String,
        memberId: List<String>,
        timestamp: Instant? = null
    ): SystemSwitchRecord?

    /**
     *
     * */
    abstract suspend fun dropSwitch(switch: SystemSwitchRecord)

    /**
     *
     * */
    abstract suspend fun updateSwitch(switch: SystemSwitchRecord)

    open suspend fun createSwitch(switch: SystemSwitchRecord) = updateSwitch(switch)

    /**
     *
     * */
    suspend fun fetchLatestSwitch(systemId: String): SystemSwitchRecord? =
        fetchSwitchesFromSystem(systemId)?.maxByOrNull {
            it.timestamp
        }

    /**
     *
     * */
    suspend fun fetchSecondLatestSwitch(systemId: String): SystemSwitchRecord? {
        val switches = fetchSortedSwitchesFromSystem(systemId)
            ?: return null

        if (switches.size < 2) return null

        return switches[1]
    }

    /**
     *
     * */
    suspend fun fetchSortedSwitchesFromSystem(
        systemId: String
    ): List<SystemSwitchRecord>? = fetchSwitchesFromSystem(systemId)?.sortedByDescending { it.timestamp }

    /**
     * Get switches by user ID
     *
     * @param userId The user ID to get all switches by.
     * @return All switches registered for the system.
     * */
    open suspend fun fetchSwitchesFromUser(
        userId: ULong
    ) = fetchUser(userId)?.systemId?.let { fetchSwitchesFromSystem(it) }

    suspend inline fun fetchSwitchesFromUser(
        user: UserBehavior?
    ) = user?.run { fetchSwitchesFromUser(id.value) }

    /**
     * Get switches by system ID
     *
     * @param systemId The system ID to get all switches by.
     * @return All switches registered for the system.
     * */
    abstract suspend fun fetchSwitchesFromSystem(
        systemId: String
    ): List<SystemSwitchRecord>?

    /**
     * Removes a proxy tag
     * @param proxyTag The proxy tag to remove
     * */
    abstract suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord)

    /**
     * Updates the trust level for the trustee
     * @param trustee The owner of the system
     * @param systemId The person being trusted
     * @param level the level of trust granted
     * */
    abstract suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean

    abstract suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel

    /**
     * Gets the total number of systems registered
     *
     * Implementation requirements: return an int with the total systems in the database
     * */
    abstract suspend fun fetchTotalSystems(): Int?

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    suspend inline fun fetchTotalMembersFromUser(user: UserBehavior?) = fetchUser(user)?.systemId?.let { fetchTotalMembersFromSystem(it) } ?: -1

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    abstract suspend fun fetchTotalMembersFromSystem(systemId: String): Int?

    /**
     * Gets a member by system ID and member name
     * */
    abstract suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String, caseSensitive: Boolean = true): MemberRecord?

    /**
     * Gets the groups a member is a part of
     * */
    abstract suspend fun fetchGroupsFromMember(member: MemberRecord): List<GroupRecord>

    /**
     * Gets the members that are a part of a group
     * */
    abstract suspend fun fetchMembersFromGroup(group: GroupRecord): List<MemberRecord>

    /**
     * Gets a member by system ID and either member ID or name.
     * */
    suspend fun findMember(systemId: String, member: String): MemberRecord? =
        if (member.startsWith("id:"))
            fetchMemberFromSystem(systemId, member.substring(3))
                ?: fetchMemberFromSystemAndName(systemId, member, false)
        else
            fetchMemberFromSystemAndName(systemId, member, true)
                ?: fetchMemberFromSystemAndName(systemId, member, false)
                ?: fetchMemberFromSystem(systemId, member)

    /**
     * Gets a member by user snowflake and member name
     * */
    suspend inline fun fetchMemberFromUserAndName(user: UserBehavior, memberName: String, caseSensitive: Boolean = true) = fetchUser(user)?.systemId?.let { fetchMemberFromSystemAndName(it, memberName, caseSensitive) }

    // === Unsafe direct-write import & export functions ===
    abstract suspend fun export(other: Database)

    /**
     * Bulk inserter for importers.
     *
     * @return The database instance capable of bulk insertions, this otherwise.
     * */
    open fun bulkInserter(): Database = this

    /**
     * Commits
     * */
    open suspend fun commit() {}

    /**
     * Effectively a transaction block for mass-editing data in any form
     *
     * @param action The transaction, either generator or otherwise.
     * */
    suspend inline fun bulk(action: Database.() -> Unit) {
        val bulk = bulkInserter()
        action(bulk)
        bulk.commit()
    }

    @TestOnly
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not for regular use.")
    abstract suspend fun drop()
    protected fun fail(message: String): Nothing = throw DatabaseException(message)

    /**
     * Checks to see if the system ID is reserved by the database in any form.
     *
     * @param systemId The system ID to check
     * @return true if the ID is reserved or unusable, false otherwise, implying non-null systemId.
     */
    @OptIn(ExperimentalContracts::class)
    protected suspend fun isSystemIdReserved(systemId: String?): Boolean {
        contract {
            returns(false) implies (systemId != null)
        }
        return !systemId.isValidPkString() || containsSystem(systemId)
    }

    abstract suspend fun firstFreeSystemId(id: String? = null): String

    /**
     * Checks to see if the member ID is reserved by the database in any form.
     *
     * @param systemId The system ID to check.
     * @param memberId The member ID to check.
     * @return true if the ID is reserved or unusable, false otherwise, implying non-null memberId.
     */
    @OptIn(ExperimentalContracts::class)
    protected suspend fun isMemberIdReserved(systemId: String, memberId: String?): Boolean {
        contract {
            returns(false) implies (memberId != null)
        }
        return !systemId.isValidPkString() || !memberId.isValidPkString() || containsMember(systemId, memberId)
    }

    open suspend fun firstFreeMemberId(systemId: String, id: String? = null): String {
        return if (isMemberIdReserved(systemId, id)) fetchMembersFromSystem(systemId)?.map(MemberRecord::id)?.firstFree() ?: "aaaaa" else id
    }
}