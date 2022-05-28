package io.github.proxyfox.database
//
//import dev.kord.common.entity.Snowflake
//import io.github.proxyfox.database.DatabaseUtils.fromPkString
//import io.github.proxyfox.database.DatabaseUtils.toPkString
//import io.github.proxyfox.database.records.member.MemberProxyTagRecord
//import io.github.proxyfox.database.records.member.MemberRecord
//import io.github.proxyfox.database.records.member.MemberServerSettingsRecord
//import io.github.proxyfox.database.records.misc.AutoProxyMode
//import io.github.proxyfox.database.records.misc.ServerSettingsRecord
//import io.github.proxyfox.database.records.system.SystemRecord
//import io.github.proxyfox.database.records.system.SystemServerSettingsRecord
//import io.github.proxyfox.database.records.system.SystemSwitchRecord
//import io.github.proxyfox.printStep
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.postgresql.Driver
//import org.slf4j.LoggerFactory
//import java.sql.Connection
//import java.sql.SQLException
//import java.sql.Types
//import java.time.OffsetDateTime
//import java.time.ZoneOffset
//import java.util.*
//
//// Created 2022-10-04T12:14:49
//
///**
// * @author KJP12
// * @since ${version}
// **/
//@Deprecated("Will be unmaintained until Hachmitsu is usable. For now, use the JSON or Mongo drivers.")
//class PostgresDatabase(val driver: Driver) : Database() {
//    private val logger = LoggerFactory.getLogger(PostgresDatabase::class.java)
//
//    private lateinit var connection: Connection
//
//    fun startConnection(uri: String, properties: Properties) {
//        try {
//            this.connection = driver.connect(uri, properties)!!
//            if (this.connection.isClosed) throw AssertionError("Database connection to $uri was closed on creation?")
//            var schema = -1
//            try {
//                val statement = connection.prepareCall("SELECT schema FROM pfmeta WHERE id = 0;")
//                val results = statement.executeQuery()
//                if (results.next()) {
//                    schema = results.getInt(1)
//                }
//            } catch (sql: SQLException) {
//                sql.printStackTrace()
//                printStep("Your database is potentially foobar. Continuing onto init.", 2)
//            }
//            if (schema == -1) {
//                connection.prepareCall(PostgresDatabase::class.java.getResourceAsStream("/assets/databases/postgres-bootstrap.pgsql")!!.reader().readText()).execute()
//            } else if (schema < 3) {
//                val statement = connection.createStatement()
//                if (schema <= 1) {
//                    statement.addBatch("ALTER TABLE members ADD birthday TEXT NULL")
//                }
//                if (schema <= 2) {
//                    statement.addBatch("ALTER TABLE memberServerPreferences ADD autoProxy BOOLEAN DEFAULT TRUE")
//                }
//                statement.addBatch("UPDATE pfmeta SET schema = 3 WHERE id = 0")
//                statement.executeBatch()
//                statement.close()
//            }
//        } catch (sql: SQLException) {
//            throw RuntimeException("Unable to create connection to $uri", sql)
//        }
//    }
//
//    override suspend fun getSystemByHost(userId: Snowflake): SystemRecord? {
//        return withContext(Dispatchers.IO) {
//            val statement = connection.prepareStatement("SELECT id, name, description, tag, avatarUrl, timezone, created FROM hosts JOIN systems ON systems.id = systemId WHERE discordId = ? ;")
//            statement.setLong(1, userId.value.toLong())
//            val results = statement.executeQuery()
//            var ret: SystemRecord? = null
//            if (results.next()) {
//                val id = results.getInt(1)
//                val name = results.getString(2)
//                val description = results.getString(3)
//                val tag = results.getString(4)
//                val avatarUrl = results.getString(5)
//                val timezone = results.getString(6)
//                val created = OffsetDateTime.ofInstant(results.getTimestamp(7).toInstant(), ZoneOffset.UTC)
//                ret = SystemRecord(id.toPkString(), name, description, tag, avatarUrl, timezone, created, null, AutoProxyMode.OFF, null)
//            }
//            // We're disposing the instance in whole, so, might as well
//            results.close()
//            statement.close()
//            ret
//        }
//    }
//
//    override suspend fun getSystemById(systemId: String): SystemRecord? {
//        return withContext(Dispatchers.IO) {
//            val statement = connection.prepareStatement("SELECT name, description, tag, avatarUrl, timezone, created FROM systems WHERE id = ?;")
//            statement.setInt(1, systemId.fromPkString())
//            val results = statement.executeQuery()
//            var ret: SystemRecord? = null
//            if (results.next()) {
//                val name = results.getString(1)
//                val description = results.getString(2)
//                val tag = results.getString(3)
//                val avatarUrl = results.getString(4)
//                val timezone = results.getString(5)
//                val created = OffsetDateTime.ofInstant(results.getTimestamp(7).toInstant(), ZoneOffset.UTC)
//                ret = SystemRecord(systemId, name, description, tag, avatarUrl, timezone, created, null, AutoProxyMode.OFF, null)
//            }
//            // We're disposing the instance in whole, so, might as well
//            results.close()
//            statement.close()
//            ret
//        }
//    }
//
//    override suspend fun getMembersByHost(userId: Snowflake) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM hosts JOIN members ON members.systemId = hosts.systemId WHERE discordId = ?;")
//        statement.setLong(1, userId.value.toLong())
//        val results = statement.executeQuery()
//        val ret = ArrayList<MemberRecord>()
//        while (results.next()) {
//            val id = results.getInt(1)
//            val systemId = results.getInt(2)
//            val name = results.getString(3)
//            val displayName = results.getString(4)
//            val description = results.getString(5)
//            val pronouns = results.getString(6)
//            val color = results.getInt(7)
//            val avatarUrl = results.getString(8)
//            val keepProxyTags = results.getBoolean(9)
//            val messageCount = results.getLong(10)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(12)
//            ret.add(MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday))
//        }
//        results.close()
//        statement.close()
//        ret.ifEmpty { null }
//    }
//
//    override suspend fun getMembersBySystem(systemId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM members WHERE systemId = ?;")
//        statement.setInt(1, systemId.fromPkString())
//        val results = statement.executeQuery()
//        val ret = ArrayList<MemberRecord>()
//        while (results.next()) {
//            val id = results.getInt(1)
//            val name = results.getString(2)
//            val displayName = results.getString(3)
//            val description = results.getString(4)
//            val pronouns = results.getString(5)
//            val color = results.getInt(6)
//            val avatarUrl = results.getString(7)
//            val keepProxyTags = results.getBoolean(8)
//            val messageCount = results.getLong(9)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(10).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(11)
//            ret.add(MemberRecord(id.toPkString(), systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday))
//        }
//        results.close()
//        statement.close()
//        ret.ifEmpty { null }
//    }
//
//    override suspend fun getMemberByHost(discordId: Snowflake, memberId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM hosts JOIN members ON members.systemId = hosts.systemId AND members.id = ? WHERE discordId = ?;")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setInt(2, memberId.fromPkString())
//        val results = statement.executeQuery()
//        var ret: MemberRecord? = null
//        if (results.next()) {
//            val id = results.getInt(1)
//            val systemId = results.getInt(2)
//            val name = results.getString(3)
//            val displayName = results.getString(4)
//            val description = results.getString(5)
//            val pronouns = results.getString(6)
//            val color = results.getInt(7)
//            val avatarUrl = results.getString(8)
//            val keepProxyTags = results.getBoolean(9)
//            val messageCount = results.getLong(10)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(12)
//            ret = MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getMemberByHostAndName(discordId: Snowflake, memberName: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM hosts JOIN members ON members.systemId = hosts.systemId AND members.name = ? WHERE discordId = ?;")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setString(2, memberName)
//        val results = statement.executeQuery()
//        var ret: MemberRecord? = null
//        if (results.next()) {
//            val id = results.getInt(1)
//            val systemId = results.getInt(2)
//            val name = results.getString(3)
//            val displayName = results.getString(4)
//            val description = results.getString(5)
//            val pronouns = results.getString(6)
//            val color = results.getInt(7)
//            val avatarUrl = results.getString(8)
//            val keepProxyTags = results.getBoolean(9)
//            val messageCount = results.getLong(10)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(12)
//            ret = MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun export(other: Database) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(memberProxyTagRecord: MemberProxyTagRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(memberRecord: MemberRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(memberServerSettingsRecord: MemberServerSettingsRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(serverSettingsRecord: ServerSettingsRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(system: SystemRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(systemServerSettingsRecord: SystemServerSettingsRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun import(systemSwitchRecord: SystemSwitchRecord) {
//        TODO("Not yet implemented")
//    }
//
//    override fun close() {
//        connection.close()
//    }
//
//    override suspend fun getMemberById(systemId: String, memberId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM members WHERE systemId = ? AND id = ?;")
//        statement.setInt(1, systemId.fromPkString())
//        statement.setInt(2, memberId.fromPkString())
//        val results = statement.executeQuery()
//        var ret: MemberRecord? = null
//        if (results.next()) {
//            val name = results.getString(1)
//            val displayName = results.getString(2)
//            val description = results.getString(3)
//            val pronouns = results.getString(4)
//            val color = results.getInt(5)
//            val avatarUrl = results.getString(6)
//            val keepProxyTags = results.getBoolean(7)
//            val messageCount = results.getLong(8)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(9).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(10)
//            ret = MemberRecord(memberId, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getMemberByIdAndName(systemId: String, memberName: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM members WHERE systemId = ? AND name = ?;")
//        statement.setInt(1, systemId.fromPkString())
//        statement.setString(2, memberName)
//        val results = statement.executeQuery()
//        var ret: MemberRecord? = null
//        if (results.next()) {
//            val id = results.getInt(1)
//            val name = results.getString(2)
//            val displayName = results.getString(3)
//            val description = results.getString(4)
//            val pronouns = results.getString(5)
//            val color = results.getInt(6)
//            val avatarUrl = results.getString(7)
//            val keepProxyTags = results.getBoolean(8)
//            val messageCount = results.getLong(9)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(10).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(11)
//            ret = MemberRecord(id.toPkString(), systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getFrontingMemberByHost(discordId: Snowflake) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT id, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday FROM hosts JOIN systems ON systems.id = systemId JOIN members ON members.systemId = hosts.systemId AND members.id = systems.autoProxy WHERE discordId = ?;")
//        statement.setLong(1, discordId.value.toLong())
//        val results = statement.executeQuery()
//        var ret: MemberRecord? = null
//        if (results.next()) {
//            val id = results.getInt(1)
//            val systemId = results.getInt(2)
//            val name = results.getString(3)
//            val displayName = results.getString(4)
//            val description = results.getString(5)
//            val pronouns = results.getString(6)
//            val color = results.getInt(7)
//            val avatarUrl = results.getString(8)
//            val keepProxyTags = results.getBoolean(9)
//            val messageCount = results.getLong(10)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(12)
//            ret = MemberRecord(id.toPkString(), systemId.toPkString(), name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getFrontingMemberByTags(discordId: Snowflake, message: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT m.id, m.systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday, startTag, endTag FROM hosts h JOIN memberProxyTags t ON t.systemId = h.systemId JOIN members m ON m.id = t.memberId AND m.systemId = h.systemId WHERE discordId = ? AND (startTag IS NULL OR position(startTag in ?) = 1) AND (endTag IS NULL OR position(reverse(endTag) in ?) = 1) ORDER BY count(startTag) + count(endTag) DESC")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setString(2, message)
//        // Reversal is required within the SQL
//        statement.setString(3, message.reversed())
//        val results = statement.executeQuery()
//        var retMember: MemberRecord? = null
//        var retString: String = message
//        if (results.next()) {
//            val memberId = results.getInt(1).toPkString()
//            val systemId = results.getInt(2).toPkString()
//            val name = results.getString(3)
//            val displayName = results.getString(4)
//            val description = results.getString(5)
//            val pronouns = results.getString(6)
//            val color = results.getInt(7)
//            val avatarUrl = results.getString(8)
//            val keepProxyTags = results.getBoolean(9)
//            val messageCount = results.getLong(10)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(11).toInstant(), ZoneOffset.UTC)
//            val birthday = results.getString(12)
//            retMember = MemberRecord(memberId, systemId, name, displayName, description, pronouns, color, avatarUrl, keepProxyTags, messageCount, created, birthday)
//
//            if (!keepProxyTags) {
//                val startTag = results.getString(13) ?: ""
//                val endTag = results.getString(14) ?: ""
//                retString = message.substring(startTag.length, message.length - endTag.length)
//            }
//        }
//        results.close()
//        statement.close()
//        retMember?.let { Pair(it, retString) }
//    }
//
//    override suspend fun getProxyTagFromMessage(discordId: Snowflake, message: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT t.systemId, t.memberId, startTag, endTag FROM hosts h JOIN memberProxyTags t ON t.systemId = h.systemId WHERE discordId = ? AND (startTag IS NULL OR position(startTag in ?) = 1) AND (endTag IS NULL OR position(reverse(endTag) in ?) = 1) ORDER BY count(startTag) + count(endTag) DESC")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setString(2, message)
//        // Reversal is required within the SQL
//        statement.setString(3, message.reversed())
//        val results = statement.executeQuery()
//        var ret: MemberProxyTagRecord? = null
//        if (results.next()) {
//            val systemId = results.getInt(1).toPkString()
//            val memberId = results.getInt(2).toPkString()
//            val startTag = results.getString(3)
//            val endTag = results.getString(4)
//            ret = MemberProxyTagRecord(systemId, memberId, startTag, endTag)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getFrontingServerSettingsByHost(serverId: Snowflake, discordId: Snowflake) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT systemId, memberId, avatarUrl, nickname, autoProxy, proxyEnabled FROM hosts JOIN systems ON systems.id = systemId JOIN memberServerPreferences msp ON msp.systemId = hosts.systemId AND msp.memberId = systems.autoProxy AND msp.serverId = ? WHERE discordId = ?;")
//        statement.setLong(1, serverId.value.toLong())
//        statement.setLong(2, discordId.value.toLong())
//        val results = statement.executeQuery()
//        var ret: MemberServerSettingsRecord? = null
//        if (results.next()) {
//            val systemId = results.getInt(1)
//            val memberId = results.getInt(2)
//            val avatarUrl = results.getString(3)
//            val nickname = results.getString(4)
//            val autoProxy = results.getBoolean(5)
//            val proxyEnabled = results.getBoolean(6)
//            ret = MemberServerSettingsRecord(serverId.value, systemId.toPkString(), memberId.toPkString(), avatarUrl, nickname, autoProxy, proxyEnabled)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake, memberId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT systemId, avatarUrl, nickname, autoProxy, proxyEnabled FROM hosts JOIN systemServerPreferences ssp ON ssp.systemId = hosts.systemId AND ssp.serverId = ? AND ssp.memberId = ? WHERE discordId = ?")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setLong(2, serverId.value.toLong())
//        statement.setInt(3, memberId.fromPkString())
//        val results = statement.executeQuery()
//        var ret: MemberServerSettingsRecord? = null
//        if (results.next()) {
//            val systemId = results.getInt(1)
//            val avatarUrl = results.getString(2)
//            val nickname = results.getString(3)
//            val autoProxy = results.getBoolean(4)
//            val proxyEnabled = results.getBoolean(5)
//            ret = MemberServerSettingsRecord(serverId.value, systemId.toPkString(), memberId, avatarUrl, nickname, autoProxy, proxyEnabled)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getServerSettingsByHost(serverId: Snowflake, discordId: Snowflake) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT systemId, proxyEnabled FROM systemServerPreferences FROM hosts JOIN systems ON systems.id = systemId WHERE serverId = ? AND discordId = ?")
//        statement.setLong(1, serverId.value.toLong())
//        statement.setLong(2, discordId.value.toLong())
//        val results = statement.executeQuery()
//        var ret: SystemServerSettingsRecord? = null
//        if (results.next()) {
//            val systemId = results.getInt(1).toPkString()
//            val proxyEnabled = results.getBoolean(2)
//            ret = SystemServerSettingsRecord(serverId.value, systemId, proxyEnabled)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getServerSettingsByMember(serverId: Snowflake, systemId: String, memberId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT avatarUrl, nickname, autoProxy, proxyEnabled FROM systemServerPreferences WHERE systemId = ? AND serverId = ? AND memberId = ?")
//        statement.setInt(1, systemId.fromPkString())
//        statement.setLong(2, serverId.value.toLong())
//        statement.setInt(3, memberId.fromPkString())
//        val results = statement.executeQuery()
//        var ret: MemberServerSettingsRecord? = null
//        if (results.next()) {
//            val avatarUrl = results.getString(1)
//            val nickname = results.getString(2)
//            val autoProxy = results.getBoolean(3)
//            val proxyEnabled = results.getBoolean(4)
//            ret = MemberServerSettingsRecord(serverId.value, systemId, memberId, avatarUrl, nickname, autoProxy, proxyEnabled)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun allocateSystem(discordId: Snowflake): SystemRecord {
//        // This ideally would be inlined
//        val system = getSystemByHost(discordId)
//        if (system != null) return system
//        return withContext(Dispatchers.IO) {
//            val statement = connection.prepareStatement("WITH ins1 AS (INSERT INTO systems(id) VALUES(DEFAULT) RETURNING id as systemId, created), ins2 AS (INSERT INTO hosts (systemId, discordId) SELECT systemId, ? FROM ins1) SELECT systemId, created FROM ins1;")
//            statement.setLong(1, discordId.value.toLong())
//            val results = statement.executeQuery()
//            if (results.next()) {
//                val systemId = results.getInt(1)
//                val created = OffsetDateTime.ofInstant(results.getTimestamp(2).toInstant(), ZoneOffset.UTC)
//                results.close()
//                statement.close()
//                return@withContext SystemRecord(systemId.toPkString(), timestamp = created)
//            } else {
//                throw IllegalStateException("$statement & $results didn't return anything?")
//            }
//        }
//    }
//
//    override suspend fun allocateMember(systemId: String, name: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("INSERT INTO members (systemId, name) VALUES (?, ?) RETURNING id, created;")
//        statement.setInt(1, systemId.fromPkString())
//        statement.setString(2, name)
//        val results = statement.executeQuery()
//        if (results.next()) {
//            val memberId = results.getInt(1)
//            val created = OffsetDateTime.ofInstant(results.getTimestamp(2).toInstant(), ZoneOffset.UTC)
//            results.close()
//            statement.close()
//            return@withContext MemberRecord(memberId.toPkString(), systemId, name, timestamp = created)
//        } else {
//            throw IllegalStateException("$statement & $results didn't return anything?")
//        }
//    }
//
//    override suspend fun updateMember(member: MemberRecord) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("UPDATE members SET (name, displayName, description, pronouns, color, avatar, keepProxy, birthday) = (?, ?, ?, ?, ?, ?, ?, ?) WHERE id = ? AND  systemId = ?;")
//        statement.setString(1, member.name)
//        statement.setString(2, member.displayName)
//        statement.setString(3, member.description)
//        statement.setString(4, member.pronouns)
//        statement.setInt(5, member.color)
//        statement.setString(6, member.avatarUrl)
//        statement.setBoolean(7, member.keepProxy)
//        statement.setInt(8, member.id.fromPkString())
//        statement.setInt(9, member.systemId.fromPkString())
//        statement.setString(10, member.birthday)
//        val i = statement.executeUpdate()
//        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $member")
//    }
//
//    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("UPDATE memberServerPreferences SET (avatarUrl, nickname, autoProxy, proxyEnabled) = (?, ?, ?) FROM members WHERE members.systemId = ? AND members.id = ? AND serverId = ? AND memberId = members.globalId;")
//        statement.setString(1, serverSettings.avatarUrl)
//        statement.setString(2, serverSettings.nickname)
//        statement.setBoolean(3, serverSettings.autoProxy)
//        statement.setBoolean(4, serverSettings.proxyEnabled)
//        statement.setInt(5, serverSettings.systemId.fromPkString())
//        statement.setInt(6, serverSettings.memberId.fromPkString())
//        statement.setLong(7, serverSettings.serverId.toLong())
//        val i = statement.executeUpdate()
//        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $serverSettings")
//    }
//
//    override suspend fun updateSystem(system: SystemRecord) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("UPDATE systems SET (name, description, tag, avatarUrl, timezone, autoProxy, autoProxyMode, autoProxyTimeout) = (?, ?, ?, ?, ?, members.globalId, ?::autoproxymode, ?) FROM systems s LEFT OUTER JOIN members ON members.systemId = s.id AND members.id = ? WHERE systems.id = ?;")
//        statement.setString(1, system.name)
//        statement.setString(2, system.description)
//        statement.setString(3, system.tag)
//        statement.setString(4, system.avatarUrl)
//        statement.setString(5, system.timezone)
//        statement.setString(6, system.autoType.name.lowercase())
//        statement.setLong(7, system.autoProxyTimeout?.inWholeMilliseconds ?: 0L)
//        statement.setInt(9, system.id.fromPkString())
//        system.autoProxy?.let { statement.setInt(8, it.fromPkString()) } ?: statement.setNull(8, Types.INTEGER)
//        val i = statement.executeUpdate()
//        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $system")
//    }
//
//    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("UPDATE systemServerPreferences SET (proxyEnabled, autoProxy, autoProxyMode, autoProxyTimeout) = (?, members.globalId, ?::autoproxymode, ?) FROM systems s LEFT OUTER JOIN members ON members.systemId = s.id AND members.id = ? WHERE serverId = ? AND systemId = ?")
//        statement.setBoolean(1, serverSettings.proxyEnabled)
//        statement.setString(2, serverSettings.autoProxyMode.name.lowercase())
//        statement.setLong(3, serverSettings.autoProxyTimeout?.inWholeMilliseconds ?: 0L)
//        statement.setLong(5, serverSettings.serverId.toLong())
//        statement.setInt(6, serverSettings.systemId.fromPkString())
//        serverSettings.autoProxy?.let { statement.setInt(4, it.fromPkString()) } ?: statement.setNull(4, Types.INTEGER)
//        val i = statement.executeUpdate()
//        if (i != 1) throw IllegalStateException("Expected 1, got $i from $statement? See $serverSettings")
//    }
//
//    override suspend fun addUserToSystem(discordId: Snowflake, systemId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("INSERT INTO hosts (discordId, systemId) VALUES (?, ?);")
//        statement.setLong(1, discordId.value.toLong())
//        statement.setInt(2, systemId.fromPkString())
//        statement.execute()
//        statement.close()
//    }
//
//    override suspend fun removeUserFromSystem(discordId: Snowflake, systemId: String) = withContext(Dispatchers.IO) {
//        val savepoint = connection.setSavepoint()
//        val statement = connection.prepareStatement("DELETE FROM hosts WHERE discordId = ? AND systemId = ?; SELECT count(*) FROM hosts WHERE systemId = ?;")
//        val sid = systemId.fromPkString()
//        statement.setLong(1, discordId.value.toLong())
//        statement.setInt(2, sid)
//        statement.setInt(3, sid)
//        val results = statement.executeQuery()
//        if (results.next() && results.getInt(1) == 0) {
//            // There's no more users attached to the system
//            val del = connection.prepareStatement("DELETE FROM systems WHERE id = ?")
//            del.setInt(1, sid)
//            val i = del.executeUpdate()
//            if (i != 1) {
//                connection.rollback(savepoint)
//                throw IllegalStateException("Expected 1, got $i from $statement? See $discordId, $systemId; rolledback.")
//            }
//        }
//        connection.releaseSavepoint(savepoint)
//    }
//
//    override suspend fun getTotalSystems() = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT count(*) FROM systems;")
//        val results = statement.executeQuery()
//        var ret: Int? = null
//        if (results.next()) {
//            ret = results.getInt(1)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getTotalMembersByHost(discordId: Snowflake) = withContext(Dispatchers.IO) {// "SELECT name, description, tag, avatarUrl, timezone, created FROM systems WHERE id = ?;"
//        val statement = connection.prepareStatement("SELECT count(*) FROM hosts JOIN members ON members.systemId = hosts.systemId WHERE discordId = ?;")
//        statement.setLong(1, discordId.value.toLong())
//        val results = statement.executeQuery()
//        var ret: Int? = null
//        if (results.next()) {
//            ret = results.getInt(1)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//
//    override suspend fun getTotalMembersById(systemId: String) = withContext(Dispatchers.IO) {
//        val statement = connection.prepareStatement("SELECT count(*) FROM members WHERE systemId = ?;")
//        statement.setInt(1, systemId.fromPkString())
//        val results = statement.executeQuery()
//        var ret: Int? = null
//        if (results.next()) {
//            ret = results.getInt(1)
//        }
//        results.close()
//        statement.close()
//        ret
//    }
//}