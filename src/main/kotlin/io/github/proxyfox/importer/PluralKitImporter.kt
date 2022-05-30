package io.github.proxyfox.importer

import io.github.proxyfox.database
import io.github.proxyfox.database.records.member.MemberProxyTagRecord
import io.github.proxyfox.database.records.member.MemberRecord
import io.github.proxyfox.database.records.system.SystemRecord
import io.github.proxyfox.fromColor
import io.github.proxyfox.toColor
import io.github.proxyfox.types.PkSystem

/**
 * [Importer] to import a JSON with a PluralKit format
 *
 * @author Oliver
 * */
class PluralKitImporter : Importer {
    private lateinit var system: SystemRecord
    private var members: List<MemberRecord> = ArrayList()
    private var proxies: HashMap<MemberRecord, List<MemberProxyTagRecord>> = HashMap()
    private var createdMembers = 0
    private var updatedMembers = 0

    override suspend fun import(string: String, userId: String) {
        val pkSystem = gson.fromJson(string, PkSystem::class.java)
        system = database.allocateSystem(userId)
        system.name = pkSystem.name ?: system.name
        system.description = pkSystem.description ?: system.description
        system.tag = pkSystem.tag ?: system.tag
        system.avatarUrl = pkSystem.avatar_url ?: system.avatarUrl
        if (pkSystem.members != null)
            for (pkMember in pkSystem.members!!) {
                var member = database.getMemberById(system.id, pkMember.name)
                if (member == null) {
                    member = database.allocateMember(system.id, pkMember.name)
                    createdMembers++
                } else updatedMembers++
                member!!.displayName = pkMember.display_name ?: member.displayName
                member.description = pkMember.description ?: member.description
                member.pronouns = pkMember.pronouns ?: member.pronouns
                member.color = (pkMember.color ?: member.color.fromColor()).toColor()
                member.keepProxy = pkMember.keep_proxy ?: member.keepProxy
                member.messageCount = pkMember.message_count ?: member.messageCount
                if (pkMember.proxies != null)
                    for (pkProxy in pkMember.proxies!!) {
                        val text = "${pkProxy.prefix}text${pkProxy.suffix}"
                        if (database.getProxyTagFromMessage(userId, text) != null) continue
                    }
            }
    }

    // Getters:
    override suspend fun getSystem(): SystemRecord = system

    override suspend fun getMembers(): List<MemberRecord> = members

    override suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> = proxies[member]!!

    override suspend fun getNewMembers(): Int = createdMembers

    override suspend fun getUpdatedMembers(): Int = updatedMembers
}

