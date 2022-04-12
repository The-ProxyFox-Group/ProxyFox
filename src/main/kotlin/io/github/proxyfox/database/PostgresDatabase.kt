package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.Utilities.fromPkString
import io.github.proxyfox.database.Utilities.toPkString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.Driver
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

// Created 2022-10-04T12:14:49

/**
 * @author KJP12
 * @since ${version}
 **/
class PostgresDatabase(val driver: Driver) : Database {
    private val logger = LoggerFactory.getLogger(PostgresDatabase::class.java)

    private lateinit var connection: Connection

    fun startConnection(uri: String, properties: Properties) {
        try {
            this.connection = driver.connect(uri, properties)!!
            if (this.connection.isClosed) throw AssertionError("Database connection to $uri was closed on creation?")
        } catch (sql: SQLException) {
            throw RuntimeException("Unable to create connection to $uri", sql)
        }
    }

    override suspend fun getSystemByHost(userId: Snowflake): SystemRecord? {
        return withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement("SELECT id, name, description, tag, avatarUrl, timezone, created FROM hosts WHERE discordId = ? JOIN systems ON systems.id = systemId;")
            statement.setLong(1, userId.value.toLong())
            val results = statement.executeQuery()
            var ret: SystemRecord? = null
            if (results.next()) {
                val id = results.getInt(1)
                val name = results.getString(2)
                val description = results.getString(3)
                val tag = results.getString(4)
                val avatarUrl = results.getString(5)
                val timezone = results.getString(6)
                val created = OffsetDateTime.ofInstant(results.getTimestamp(7).toInstant(), ZoneOffset.UTC)
                ret = SystemRecord(id.toPkString(), name, description, tag, avatarUrl, timezone, created, null, AutoProxyMode.OFF, null)
            }
            results.close()
            ret
        }
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement("SELECT name, description, tag, avatarUrl, timezone, created FROM systems WHERE id = ?;")
            statement.setInt(1, systemId.fromPkString())
            val results = statement.executeQuery()
            if (results.next()) {
                // TODO:
            }
        }
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

    override suspend fun getFrontingMemberByTags(discordId: Snowflake, message: String): MemberRecord? {
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

    override suspend fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String): MemberServerSettingsRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun allocateSystem(discordId: Snowflake): SystemRecord? {
        TODO("Not yet implemented")
    }

    override suspend fun allocateMember(systemId: String, name: String): MemberRecord? {
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
        TODO("Not yet implemented")
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
}