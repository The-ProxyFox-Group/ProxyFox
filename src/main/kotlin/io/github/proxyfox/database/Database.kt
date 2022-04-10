package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake

// Created 2022-09-04T14:06:39

/**
 * The generic database interface for ProxyFox.
 *
 * @author KJP12
 **/
interface Database {
    // === Systems ===
    /**
     * Gets a [system][SystemRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return The system tied to the Discord user.
     * */
    fun getSystemByHost(userId: Snowflake): SystemRecord?

    /**
     * Gets a [system][SystemRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return The system as registered by ID.
     * */
    fun getSystemById(systemId: String): SystemRecord?

    // === Members ===
    /**
     * Gets a list of [members][MemberRecord] by Discord ID.
     *
     * @param userId The ID of the Discord user.
     * @return A list of members registered to the system tied to the Discord user.
     * */
    fun getMembersByHost(userId: Snowflake): List<MemberRecord>?

    /**
     * Gets a list of [members][MemberRecord] by system ID.
     *
     * @param systemId The ID of the system.
     * @return A list of members registered to the system.
     * */
    fun getMembersBySystem(systemId: String): List<MemberRecord>?

    /**
     * Gets the [member][MemberRecord] by both Discord & member IDs.
     *
     * @param discordId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member of the system tied to the Discord user.
     * */
    fun getMemberByHost(discordId: Snowflake, memberId: String): MemberRecord?

    /**
     * Gets the [member][MemberRecord] by both system & member IDs.
     *
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system.
     * @return The member of the system.
     * */
    fun getMemberById(systemId: String, memberId: String): MemberRecord?

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID.
     *
     * @param discordId The ID of the Discord user.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    fun getFrontingMemberByHost(discordId: Snowflake): MemberRecord?

    /**
     * Gets the fronting [member][MemberRecord] by Discord ID and proxy tags.
     *
     * @param discordId The ID of the Discord user.
     * @param message The message to check proxy tags against.
     * @return The fronting member of the system tied to the Discord user, if applicable.
     * */
    fun getFrontingMemberByTags(discordId: Snowflake, message: String): MemberRecord?

    // === Server Settings ===
    /**
     * Gets the current fronting [member's server settings][MemberServerSettingsRecord] by server & Discord IDs.
     *
     * @param serverId The ID of the server.
     * @param discordId The ID of the Discord user.
     * @return The fronting member's settings for the server.
     * */
    fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake): MemberServerSettingsRecord?

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, Discord & member IDs.
     *
     * @param serverId The ID of the server.
     * @param discordId The ID of the Discord user.
     * @param memberId The ID of the member in the system tied to the Discord user.
     * @return The member's settings for the server.
     * */
    fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake, memberId: String): MemberServerSettingsRecord?

    /**
     * Gets the [member's server settings][MemberServerSettingsRecord] by server, system & member IDs.
     *
     * @param serverId The ID of the server.
     * @param systemId The ID of the system.
     * @param memberId The ID of the member in the system.
     * @return The member's settings for the server.
     * */
    fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String): MemberServerSettingsRecord?

    // === Management ===
    /**
     * Allocates or reuses a system ID in the database.
     *
     * @param discordId The ID of the Discord user.
     * @return A maybe newly created system.
     * */
    fun allocateSystem(discordId: Snowflake): SystemRecord?

    /**
     * Allocates a member ID in the database.
     *
     * @param systemId The ID of the system.
     * @param name The name of the new member.
     * @return A newly created member ID.
     * */
    fun allocateMember(systemId: String, name: String): MemberRecord?

    fun updateMember(member: MemberRecord)
    fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord)
    fun updateSystem(system: SystemRecord)
    fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord)

    /**
     * Adds a Discord account to a system.
     *
     * Implementation requirements: Deny addition when the user is already tied to a system.
     *
     * @param discordId The ID of the Discord user.
     * @param systemId The ID of the system.
     * */
    fun addUserToSystem(discordId: Snowflake, systemId: String)

    /**
     * Removes a Discord account from a system.
     *
     * Implementation requirements: Deny removal when that is the system's only user.
     *
     * @param discordId The ID of the Discord user.
     * @param systemId The ID of the system.
     * */
    fun removeUserFromSystem(discordId: Snowflake, systemId: String)

    /**
     * Gets the total number of systems registered
     *
     * Implementation requirements: return an int with the total systems in the database
     * */
    fun getTotalSystems(): Int?

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    fun getTotalMembersByHost(discordId: Snowflake): Int?

    /**
     * Gets the total number of members registered in a system by discord ID.
     *
     * Implementation requirements: return an int with the total members registered
     * */
    fun getTotalMembersById(systemId: String): Int?
}