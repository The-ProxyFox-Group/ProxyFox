package dev.proxyfox.exporter

import dev.kord.common.entity.Snowflake
import dev.proxyfox.database.database
import dev.proxyfox.importer.gson
import dev.proxyfox.types.PkMember
import dev.proxyfox.types.PkProxy
import dev.proxyfox.types.PkSystem
import dev.proxyfox.common.fromColor
import dev.proxyfox.database.Database

object Exporter {
    suspend fun export(userId: Snowflake): String {
        val system = database.getSystemByHost(userId.value.toString()) ?: return ""

        val pkSystem = PkSystem()
        pkSystem.name = system.name
        pkSystem.description = system.description
        pkSystem.tag = system.tag
        pkSystem.avatar_url = system.avatarUrl

        val members = database.getMembersBySystem(system.id) ?: ArrayList()
        pkSystem.members = Array(members.size) {
            val member = members[it]
            val pkMember = PkMember()
            pkMember.name = member.name
            pkMember.display_name = member.displayName
            pkMember.description = member.description
            pkMember.pronouns = member.pronouns
            pkMember.color = member.color.fromColor()
            pkMember.keep_proxy = member.keepProxy
            pkMember.message_count = member.messageCount

            val proxies = database.getProxiesByIdAndMember(system.id, member.id)
            pkMember.proxies = Array(proxies!!.size) {
                val proxy = proxies[it]
                val pkProxy = PkProxy()
                pkProxy.prefix = proxy.prefix
                pkProxy.suffix = proxy.suffix
                pkProxy
            }

            pkMember
        }

        return gson.toJson(pkSystem)
    }

    suspend fun export(database: Database, userId: String): String {
        val system = database.getSystemByHost(userId) ?: return ""

        val pkSystem = PkSystem()
        pkSystem.name = system.name
        pkSystem.description = system.description
        pkSystem.tag = system.tag
        pkSystem.avatar_url = system.avatarUrl

        val members = database.getMembersBySystem(system.id) ?: ArrayList()
        pkSystem.members = Array(members.size) {
            val member = members[it]
            val pkMember = PkMember()
            pkMember.name = member.name
            pkMember.display_name = member.displayName
            pkMember.description = member.description
            pkMember.pronouns = member.pronouns
            pkMember.color = member.color.fromColor()
            pkMember.keep_proxy = member.keepProxy
            pkMember.message_count = member.messageCount
            pkMember.avatar_url = member.avatarUrl

            val proxies = database.getProxiesByIdAndMember(system.id, member.id)
            val pkProxies = ArrayList<PkProxy>()
            if (proxies != null) {
                for (proxy in proxies) {
                    val pkProxy = PkProxy()
                    pkProxy.prefix = proxy.prefix
                    pkProxy.suffix = proxy.suffix
                    pkProxies.add(pkProxy)
                }
            }
            pkMember.proxies = pkProxies.toTypedArray()

            pkMember
        }
        return gson.toJson(pkSystem)
    }
}