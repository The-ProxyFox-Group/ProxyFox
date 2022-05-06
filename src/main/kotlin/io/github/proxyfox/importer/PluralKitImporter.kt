package io.github.proxyfox.importer

import io.github.proxyfox.database.AutoProxyMode
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.database.records.system.SystemSwitchRecord
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * [Importer] to import a JSON with a PluralKit format
 *
 * @author Oliver
 * */
class PluralKitImporter : Importer {
    private lateinit var system: SystemRecord
    private var members: HashMap<String, MemberRecord> = HashMap()
    private var proxies: HashMap<String,List<MemberProxyTagRecord>> = HashMap()

    override suspend fun import(map: Map<String, *>) {
        system = SystemRecord(
            "aaaaa",
            map["name"] as String?,
            map["description"] as String?,
            map["tag"] as String?,
            map["avatar_url"] as String?,
            map["timezone"] as String?,
            OffsetDateTime.parse(map["created"] as String, DateTimeFormatter.ISO_DATE_TIME),
            null,
            AutoProxyMode.OFF,
            null
        )
        for (memMap in map["members"] as List<Map<String,*>>) {
            val member = MemberRecord(
                memMap["id"] as String,
                "aaaaa",
                memMap["name"] as String,
                memMap["display_name"] as String?,
                memMap["description"] as String?,
                memMap["pronouns"] as String?,
                memMap["color"]?.let { Integer.parseInt(it as String, 16) } ?: -1,
                memMap["avatar_url"] as String?,
                memMap["keep_proxy"] as Boolean,
                memMap["message_count"] as Long,
                OffsetDateTime.parse(memMap["created"] as String, DateTimeFormatter.ISO_DATE_TIME),
            )

            val memberSwitches = ArrayList<SystemSwitchRecord>()
            for (proxyMap in memMap["proxy_tags"] as List<Map<String,*>>) {
                val proxy = MemberProxyTagRecord(
                    "aaaaa",
                    memMap["id"] as String,
                    proxyMap["prefix"] as String?,
                    proxyMap["suffix"] as String?
                )
            }

            members.put(memMap["id"] as String, member)
        }
    }

    override suspend fun finalizeImport() {

    }

    // Getters:
    override suspend fun getSystem(): SystemRecord = system

    override suspend fun getMembers(): List<MemberRecord> {
        val memberList = ArrayList<MemberRecord>()
        for (member in members.values)
            memberList.add(member)
        return memberList
    }

    override suspend fun getMemberProxyTags(id: String): List<MemberProxyTagRecord> = proxies[id]!!
}