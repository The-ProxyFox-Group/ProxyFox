package io.github.proxyfox.database

import dev.kord.common.entity.Snowflake
import io.github.proxyfox.database.DatabaseUtils.fromPkString
import io.github.proxyfox.database.DatabaseUtils.toPkString
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
import io.github.proxyfox.database.records.misc.AutoProxyMode
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
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
            var cont = -1
            try {
                val statement = connection.prepareCall("SELECT schema FROM pfmeta WHERE id = 0;")
                val results = statement.executeQuery()
                if (results.next()) {
                    cont = results.getInt(1)
                }
            } catch (sql: SQLException) {
                sql.printStackTrace()
                println("Your database is potentially foobar. Continuing onto init.")
            }
            // For now, it'll only contain the one branch; this is preparation for any kind of migration later on.
            when (cont) {
                -1 -> connection.prepareCall(PostgresDatabase::class.java.getResourceAsStream("/assets/databases/postgres-bootstrap.pgsql")!!.reader().readText()).execute()
            }
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
            // We're disposing the instance in whole, so, might as well
            results.close()
            statement.close()
            ret
        }
    }

    override suspend fun getSystemById(systemId: String): SystemRecord? {
        return withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement("SELECT name, description, tag, avatarUrl, timezone, created FROM systems WHERE id = ?;")
            statement.setInt(1, systemId.fromPkString())
            val results = statement.executeQuery()
            var ret: SystemRecord? = null
            if (results.next()) {
                val name = results.getString(1)
                val description = results.getString(2)
                val tag = results.getString(3)
                val avatarUrl = results.getString(4)
                val timezone = results.getString(5)
                val created = OffsetDateTime.ofInstant(results.getTimestamp(7).toInstant(), ZoneOffset.UTC)
                ret = SystemRecord(systemId, name, description, tag, avatarUrl, timezone, created, null, AutoProxyMode.OFF, null)
            }
            // We're disposing the instance in whole, so, might as well
            results.close()
            statement.close()
            ret
        }
    }

    override suspend fun getMembersByHost(userId: Snowflake) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created FROM hosts WHERE discordId = ? JOIN members ON members.systemId = systemId;")
        statement.setLong(1, userId.value.toLong())
        val results = statement.executeQuery()
        val ret = ArrayList<MemberRecord>()
        while (results.next()) {
            val id = results.getInt(1)
            val systemId = results.getInt(2)
            val name = results.getString(3)
            val displayName = results.getString(4)
            val description = results.getString(5)
            val pronouns = results.getString(6)
            val color = results.getInt(7)
            val avatarUrl = results.getString(8)
            val keepProxyTags = results.getBoolean(9)
            val messageCount = results.getLong(10)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
            ret.add(MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created))
        }
        results.close()
        statement.close()
        ret.ifEmpty { null }
    }

    override suspend fun getMembersBySystem(systemId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT id, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created FROM members WHERE systemId = ?;")
        statement.setInt(1, systemId.fromPkString())
        val results = statement.executeQuery()
        val ret = ArrayList<MemberRecord>()
        while (results.next()) {
            val id = results.getInt(1)
            val name = results.getString(2)
            val displayName = results.getString(3)
            val description = results.getString(4)
            val pronouns = results.getString(5)
            val color = results.getInt(6)
            val avatarUrl = results.getString(7)
            val keepProxyTags = results.getBoolean(8)
            val messageCount = results.getLong(9)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(10).toInstant(), ZoneOffset.UTC)
            ret.add(MemberRecord(id.toPkString(), systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created))
        }
        results.close()
        statement.close()
        ret.ifEmpty { null }
    }

    override suspend fun getMemberByHost(discordId: Snowflake, memberId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created FROM hosts WHERE discordId = ? JOIN members ON members.systemId = systemId AND members.id = ?;")
        statement.setLong(1, discordId.value.toLong())
        statement.setInt(2, memberId.fromPkString())
        val results = statement.executeQuery()
        var ret: MemberRecord? = null
        if (results.next()) {
            val id = results.getInt(1)
            val systemId = results.getInt(2)
            val name = results.getString(3)
            val displayName = results.getString(4)
            val description = results.getString(5)
            val pronouns = results.getString(6)
            val color = results.getInt(7)
            val avatarUrl = results.getString(8)
            val keepProxyTags = results.getBoolean(9)
            val messageCount = results.getLong(10)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
            ret = MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun getMemberById(systemId: String, memberId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created FROM members WHERE systemId = ? AND id = ?;")
        statement.setInt(1, systemId.fromPkString())
        statement.setInt(2, memberId.fromPkString())
        val results = statement.executeQuery()
        var ret: MemberRecord? = null
        if (results.next()) {
            val name = results.getString(1)
            val displayName = results.getString(2)
            val description = results.getString(3)
            val pronouns = results.getString(4)
            val color = results.getInt(5)
            val avatarUrl = results.getString(6)
            val keepProxyTags = results.getBoolean(7)
            val messageCount = results.getLong(8)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(9).toInstant(), ZoneOffset.UTC)
            ret = MemberRecord(memberId, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun getFrontingMemberByHost(discordId: Snowflake) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created FROM hosts WHERE discordId = ? JOIN systems ON systems.id = systemId JOIN members ON members.systemId = systemId AND members.id = systems.autoProxy;")
        statement.setLong(1, discordId.value.toLong())
        val results = statement.executeQuery()
        var ret: MemberRecord? = null
        if (results.next()) {
            val id = results.getInt(1)
            val systemId = results.getInt(2)
            val name = results.getString(3)
            val displayName = results.getString(4)
            val description = results.getString(5)
            val pronouns = results.getString(6)
            val color = results.getInt(7)
            val avatarUrl = results.getString(8)
            val keepProxyTags = results.getBoolean(9)
            val messageCount = results.getLong(10)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
            ret = MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created)
        }
        results.close()
        statement.close()
        ret
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

    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake, memberId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT systemId, avatarUrl, nickname, proxyEnabled FROM hosts WHERE discordId = ? JOIN systemServerPreferences AS ssp ON ssp.systemId = systemId AND ssp.serverId = ? AND ssp.memberId = ?")
        statement.setLong(1, discordId.value.toLong())
        statement.setLong(2, serverId.value.toLong())
        statement.setInt(3, memberId.fromPkString())
        val results = statement.executeQuery()
        var ret: MemberServerSettingsRecord? = null
        if (results.next()) {
            val systemId = results.getInt(1)
            val avatarUrl = results.getString(2)
            val nickname = results.getString(3)
            val proxyEnabled = results.getBoolean(4)
            ret = MemberServerSettingsRecord(serverId.value, systemId.toPkString(), memberId, avatarUrl, nickname, proxyEnabled)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT avatarUrl, nickname, proxyEnabled FROM systemServerPreferences WHERE systemId = ? AND serverId = ? AND memberId = ?")
        statement.setInt(1, systemId.fromPkString())
        statement.setLong(2, serverId.value.toLong())
        statement.setInt(3, memberId.fromPkString())
        val results = statement.executeQuery()
        var ret: MemberServerSettingsRecord? = null
        if (results.next()) {
            val avatarUrl = results.getString(1)
            val nickname = results.getString(2)
            val proxyEnabled = results.getBoolean(3)
            ret = MemberServerSettingsRecord(serverId.value, systemId, memberId, avatarUrl, nickname, proxyEnabled)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun allocateSystem(discordId: Snowflake): SystemRecord {
        // This ideally would be inlined
        val system = getSystemByHost(discordId)
        if (system != null) return system
        return withContext(Dispatchers.IO) {
            val statement = connection.prepareStatement("WITH ins1 AS (INSERT INTO system() VALUES() RETURNING systemId, created) INSERT INTO hosts (systemId, discordId) SELECT systemId, ? FROM ins1 RETURNING systemId, created;")
            statement.setLong(1, discordId.value.toLong())
            val results = statement.executeQuery()
            if (results.next()) {
                val systemId = results.getInt(1)
                val created = OffsetDateTime.ofInstant(results.getTimestamp(2).toInstant(), ZoneOffset.UTC)
                results.close()
                statement.close()
                return@withContext SystemRecord(systemId.toPkString(), created = created)
            } else {
                throw IllegalStateException("$statement & $results didn't return anything?")
            }
        }
    }

    override suspend fun allocateMember(systemId: String, name: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("INSERT INTO members (systemId, name) VALUES (?, ?) RETURNING id, created;")
        statement.setInt(1, systemId.fromPkString())
        statement.setString(2, name)
        val results = statement.executeQuery()
        if (results.next()) {
            val memberId = results.getInt(1)
            val created = OffsetDateTime.ofInstant(results.getTimestamp(2).toInstant(), ZoneOffset.UTC)
            results.close()
            statement.close()
            return@withContext MemberRecord(memberId.toPkString(), systemId, name, created = created)
        } else {
            throw IllegalStateException("$statement & $results didn't return anything?")
        }
    }

    override suspend fun updateMember(member: MemberRecord) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("UPDATE members SET (name, displayName, description, pronouns, color, avatar, keepProxy) = (?, ?, ?, ?, ?, ?, ?) WHERE id = ? AND  systemId = ?;")
        statement.setString(1, member.name)
        statement.setString(2, member.displayName)
        statement.setString(3, member.description)
        statement.setString(4, member.pronouns)
        statement.setInt(5, member.color)
        statement.setString(6, member.avatarUrl)
        statement.setBoolean(7, member.keepProxy)
        statement.setInt(8, member.id.fromPkString())
        statement.setInt(9, member.systemId.fromPkString())
        val i = statement.executeUpdate()
        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $member")
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("UPDATE memberServerPreferences SET (avatarUrl, nickname, proxyEnabled) = (?, ?, ?) FROM members WHERE members.systemId = ? AND members.id = ? AND serverId = ? AND memberId = members.globalId;")
        statement.setString(1, serverSettings.avatarUrl)
        statement.setString(2, serverSettings.nickname)
        statement.setBoolean(3, serverSettings.proxyEnabled)
        statement.setInt(4, serverSettings.systemId.fromPkString())
        statement.setInt(5, serverSettings.memberId.fromPkString())
        statement.setLong(6, serverSettings.serverId.toLong())
        val i = statement.executeUpdate()
        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $serverSettings")
    }

    override suspend fun updateSystem(system: SystemRecord) {
        val statement = connection.prepareStatement("UPDATE systems SET (name, description, tag, avatarUrl, timezone, autoProxy, autoProxyMode, autoProxyTimeout) = (?, ?, ?, ?, ?, members.globalId, ?::autoproxymode, ?) FROM systems WHERE id = ?;")
        statement.setString(1, system.name)
        statement.setString(2, system.description)
        statement.setString(3, system.tag)
        statement.setString(4, system.avatarUrl)
        statement.setString(5, system.timezone)
        statement.setString(6, system.autoProxyMode.name)
        statement.setLong(7, system.autoProxyTimeout?.inWholeMilliseconds ?: 0L)
        statement.setInt(8, system.id.fromPkString())
        val i = statement.executeUpdate()
        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $system")
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun addUserToSystem(discordId: Snowflake, systemId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("INSERT INTO hosts (discordId, systemId) VALUES (?, ?);")
        statement.setLong(1, discordId.value.toLong())
        statement.setInt(2, systemId.fromPkString())
        statement.execute()
        statement.close()
    }

    override suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getTotalSystems() = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT count(*) FROM systems;")
        val results = statement.executeQuery()
        var ret: Int? = null
        if (results.next()) {
            ret = results.getInt(1)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun getTotalMembersByHost(discordId: Snowflake) = withContext(Dispatchers.IO) {// "SELECT name, description, tag, avatarUrl, timezone, created FROM systems WHERE id = ?;"
        val statement = connection.prepareStatement("SELECT count(*) FROM hosts WHERE discordId = ? JOIN members ON members.systemId = systemId;")
        statement.setLong(1, discordId.value.toLong())
        val results = statement.executeQuery()
        var ret: Int? = null
        if (results.next()) {
            ret = results.getInt(1)
        }
        results.close()
        statement.close()
        ret
    }

    override suspend fun getTotalMembersById(systemId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement("SELECT count(*) FROM members WHERE systemId = ?;")
        statement.setInt(1, systemId.fromPkString())
        val results = statement.executeQuery()
        var ret: Int? = null
        if (results.next()) {
            ret = results.getInt(1)
        }
        results.close()
        statement.close()
        ret
    }
}