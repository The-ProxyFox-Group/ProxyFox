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

// Created 2022-09-04T14:06:39

/**
 * The generic database interface for ProxyFox.
 *
 * @author KJP12
 **/
abstract class Database : AutoCloseable {
    // === Systems ===
    /**
     * Gets a [system][SystemRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The system tied to the Discord user.
     * */
    abstract suspend fun getSystemByHost(userId: Snowflake): SystemRecord?

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
    abstract suspend fun getMembersByHost(userId: Snowflake): List<MemberRecord>?

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
     * @param discordId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member of the system tied to the Discord user.
     * */
    abstract suspend fun getMemberByHost(discordId: Snowflake, memberId: String): MemberRecord?

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
     * @param discordId The ID of the Discord user.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    abstract suspend fun getFrontingMemberByHost(discordId: Snowflake): MemberRecord?

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID and proxy tags.
     *
     * @param discordId The ID of the Discord user.
     * @param message The message to check proxy tags against.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    abstract suspend fun getFrontingMemberByTags(discordId: Snowflake, message: String): Pair<MemberRecord, String>?

    /**
     * Gets the [proxy][MemberProxyTagRecord] by Discord ID and proxy tags.
     *
     * @param discordId The ID of the Discord user.
     * @param message The message to check proxy tags against.
     * @return The ProxyTag associated with the message
     * */
    abstract suspend fun getProxyTagFromMessage(discordId: Snowflake, message: String): MemberProxyTagRecord?

    // === Server Settings ===
    /**
     * Gets the current fronting [member's server settings][MemberServerSettingsRecord] by server & Discord IDs.
     *
     * @param serverId The ID of the server.
     * @param discordId The ID of the Discord user.
     * @return The fronting member's settings for the server.
     * */
    abstract suspend fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): MemberServerSettingsRecord?

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, Discord & member IDs.
     *
     * @param serverId The ID of the server.
     * @param discordId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    abstract suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake, memberId: String): MemberServerSettingsRecord?

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, system & member IDs.
     *
     * @param serverId The ID of the server.
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system.
     * @return The member's settings for the server.
     * */
    abstract suspend fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String): MemberServerSettingsRecord?

    /**
     * Gets the [system's server settings][SystemServerSettingsRecord] by server & Discord IDs.
     *
     * @param serverId The ID of the server.
     * @param discordId The ID of the Discord user.
     * @return The system's settings for the server.
     * */
    abstract suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): SystemServerSettingsRecord?

    // === Management ===
    /**
     * Allocates or reuses a system ID in the database.
     *
     * @param discordId The ID of the Discord user.
     * @return A maybe newly created system. Never null.
     * */
    abstract suspend fun allocateSystem(discordId: Snowflake): SystemRecord

    /**
     * Allocates a member ID in the database.
     *
     * @param systemId The ID of the system.
     * @param name The name of the new member.
     * @return A newly created member. Never null.
     * */
    abstract suspend fun allocateMember(systemId: String, name: String): MemberRecord

    // TODO: This ideally needs a much better system for updating since this really isn't ideal as is.
    //  This applies to the following 4 methods below.
    abstract suspend fun updateMember(member: MemberRecord)
    abstract suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord)
    abstract suspend fun updateSystem(system: SystemRecord)
    abstract suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord)

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
        prefix: String,
        suffix: String
    ): MemberProxyTagRecord?

    /**
     * Removes a proxy tag
     * @param systemId The system to remove it from
     * @param proxyTag The proxy tag to remove
     * */
    abstract suspend fun removeProxyTag(systemId: String, proxyTag: MemberProxyTagRecord)

    /**
     * Adds a Discord account to a system.
     *
     * Implementation requirements: Deny addition when the user is already tied to a system.
     *
     * @param discordId The ID of the Discord user.
     * @param systemId The ID of the system.
     * */
    abstract suspend fun addUserToSystem(discordId: Snowflake, systemId: String)

    /**
     * Removes a Discord account from a system.
     *
     * Implementation requirements: Delete the system if there's no users left.
     *
     * @param discordId The ID of the Discord user.
     * @param systemId The ID of the system.
     * */
    abstract suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String)

    /**
     * Updates the trust level for the trustee
     * @param userId The owner of the system
     * @param trustee The person being trusted
     * @param level the level of trust granted
     * */
    abstract suspend fun updateTrustLevel(userId: Snowflake, trustee: Snowflake, level: TrustLevel)

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
    abstract suspend fun getTotalMembersByHost(discordId: Snowflake): Int?

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
     * Gets a member by user snowflake and member name
     * */
    abstract suspend fun getMemberByHostAndName(discordId: Snowflake, memberName: String): MemberRecord?

    // === Unsafe direct-write import & export functions ===
    abstract suspend fun export(other: Database)

    // Warning: These methods may directly allocate as part of importing records, inadvertently overwriting records in the process.
    // It is not assumed that these can be used for importing TupperBox, PluralKit and ProxyFox exports.
    protected abstract suspend fun import(memberProxyTagRecord: MemberProxyTagRecord)
    protected abstract suspend fun import(memberRecord: MemberRecord)
    protected abstract suspend fun import(memberServerSettingsRecord: MemberServerSettingsRecord)
    protected abstract suspend fun import(serverSettingsRecord: ServerSettingsRecord)
    protected abstract suspend fun import(system: SystemRecord)
    protected abstract suspend fun import(systemServerSettingsRecord: SystemServerSettingsRecord)
    protected abstract suspend fun import(systemSwitchRecord: SystemSwitchRecord)
}