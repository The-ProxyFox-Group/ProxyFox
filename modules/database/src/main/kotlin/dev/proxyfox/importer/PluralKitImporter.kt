/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.importer

import com.google.gson.JsonObject
import dev.proxyfox.common.toColor
import dev.proxyfox.database.*
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import dev.proxyfox.types.PkMember
import dev.proxyfox.types.PkSystem
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * [Importer] to import a JSON with a PluralKit format
 *
 * @param directAllocation Whether to directly allocate IDs where possible.
 * @param ignoreUnfinished Whether to ignore unfinished features from PFv1 that were inadvertently included via imports.
 *                         Includes ignoring groups, subsystems and privacy settings.
 * @author Oliver
 * */
open class PluralKitImporter protected constructor(
    private val directAllocation: Boolean,
    private val ignoreUnfinished: Boolean,
) : Importer {
    private var proxies: HashMap<MemberRecord, List<MemberProxyTagRecord>> = HashMap()
    private var seenMemberIds = HashSet<String>()

    private var id = 0

    constructor() : this(false, false)

    override suspend fun import(database: Database, json: JsonObject, userId: ULong) {
        import(database, gson.fromJson(json, PkSystem::class.java), userId)
    }

    suspend fun import(database: Database, pkSystem: PkSystem, userId: ULong) {
        val fresh: Boolean
        database.fetchSystemFromUser(userId).let {
            if (it == null) {
                fresh = true
                system = database.getOrCreateSystem(userId, id = if (directAllocation) pkSystem.id else null)
            } else {
                fresh = false
                system = it
            }
        }
        system.name = pkSystem.name.sanitise() ?: system.name
        system.description = pkSystem.description.sanitise() ?: system.description
        system.tag = pkSystem.tag.sanitise() ?: system.tag
        system.avatarUrl = pkSystem.avatar_url.sanitise() ?: system.avatarUrl

        @Suppress("DEPRECATION")
        run {
            pkSystem.autoBool?.let { system.autoType = if (it) AutoProxyMode.FRONT else AutoProxyMode.OFF }
            pkSystem.serverProxyEnabled?.let {
                // Take the fast path
                if (fresh) {
                    for ((sid, enabled) in it) {
                        database.createSystemServerSettings(SystemServerSettingsRecord().apply {
                            serverId = sid
                            systemId = system.id
                            proxyEnabled = enabled
                        })
                    }
                } else {
                    for ((sid, enabled) in it) {
                        val record = database.getOrCreateServerSettingsFromSystem(sid, system.id)
                        record.proxyEnabled = enabled
                        database.updateSystemServerSettings(record)
                    }
                }
            }
        }

        pkSystem.pronouns.sanitise()?.let { system.pronouns = it }
        try {
            pkSystem.color?.validate("system/color")?.toColor()?.let { system.color = it }
        } catch (nfe: NumberFormatException) {
            throw ImporterException("Invalid colour given for system: ${pkSystem.color}", nfe)
        }
        pkSystem.config?.timezone.sanitise()?.let { system.timezone = it }

        if (directAllocation) {
            pkSystem.proxyfox?.run {
                autoProxy?.let { system.autoProxy = it }
                autoType?.let { system.autoType = it }
                trust?.let(system.trust::putAll)
            }
            pkSystem.accounts?.let(system.users::addAll)
            pkSystem.created.tryParseOffsetTimestamp()?.let { system.timestamp = it }
        }

        database.updateSystem(system)

        val idMap = HashMap<String?, String>()

        if (pkSystem.members != null) {
            val proxyTags = database.fetchProxiesFromSystem(system.id)?.toHashSet()
            val allocatedIds = HashSet<String>()
            if (!fresh) {
                val ids = database.fetchMembersFromSystem(system.id)!!.mapTo(allocatedIds, MemberRecord::id)
                // Set first free ID to here.
                id = ids.firstFreeRaw()
            }
            val birthdays = findBirthdays(pkSystem.members)

            for (pkMember in pkSystem.members) {
                val freshMember: Boolean
                val memberName = pkMember.name.sanitise().ifEmptyThen(pkMember.id) ?: findNextId(allocatedIds)
                val member = run {
                    if (!fresh) {
                        val record = pkMember.id?.let { database.fetchMemberFromSystem(system.id, it) }
                            ?: database.fetchMemberFromSystemAndName(system.id, memberName, caseSensitive = false)
                        if (record != null && seenMemberIds.add(record.id)) {
                            assert(record.name == memberName) { "$record did not match $pkMember" }
                            freshMember = false
                            updatedMembers++
                            return@run record
                        }
                    }
                    freshMember = true
                    createdMembers++
                    return@run MemberRecord(
                        if (directAllocation) pkMember.id.validateId(idMap, allocatedIds) else findNextId(allocatedIds),
                        system.id,
                        memberName,
                    )
                    /*
                    database.getOrCreateMember(system.id, memberName, id = if (directAllocation) pkMember.id else null)?.apply { createdMembers++ }
                    */
                }
                seenMemberIds.add(member.id)
                idMap[pkMember.id] = member.id

                member.displayName = pkMember.display_name.sanitise() ?: member.displayName
                member.avatarUrl = pkMember.avatar_url.sanitise() ?: member.avatarUrl
                member.description = pkMember.description.sanitise() ?: member.description
                member.pronouns = pkMember.pronouns.sanitise() ?: member.pronouns
                try {
                    member.color = pkMember.color?.sanitise()?.toColor() ?: member.color
                } catch (nfe: NumberFormatException) {
                    throw ImporterException("Invalid colour given for member ${pkMember.id} (${pkMember.name}): ${pkSystem.color}", nfe)
                }
                member.keepProxy = pkMember.keep_proxy ?: member.keepProxy
                member.messageCount = pkMember.message_count?.toULong() ?: member.messageCount
                birthdays[pkMember]?.let { member.birthday = it }

                pkMember.proxyfox?.let { proxyfox ->
                    proxyfox.age?.let { member.age = it }
                    proxyfox.role?.let { member.role = it }
                }

                pkMember.proxy_tags?.apply {
                    val memberTags = proxyTags?.filterTo(HashSet()) { it.memberId == member.id } ?: HashSet()
                    forEach { pkProxy ->
                        val proxy = MemberProxyTagRecord(system.id, member.id, pkProxy.prefix, pkProxy.suffix)
                        if (!memberTags.remove(proxy)) {
                            proxyTags?.filter { it.isEqual(proxy) }?.forEach { database.dropProxyTag(it) }
                            database.createProxyTag(proxy)
                        }
                    }
                    memberTags.forEach { database.dropProxyTag(it) }
                }
                if (directAllocation) {
                    pkMember.created.tryParseOffsetTimestamp()?.let { member.timestamp = it }
                }

                val memberServerSettings = HashMap<ULong, PfMemberServerSettings>()
                @Suppress("DEPRECATION")
                pkMember.serverAvatars?.forEach { (sid, avatar) ->
                    if (!avatar.sanitise().isNullOrBlank()) {
                        memberServerSettings.computeIfAbsent(sid) { PfMemberServerSettings() }.avatar = avatar
                    }
                }
                @Suppress("DEPRECATION")
                pkMember.serverNicknames?.forEach { (sid, nickname) ->
                    if (!nickname.sanitise().isNullOrBlank()) {
                        memberServerSettings.computeIfAbsent(sid) { PfMemberServerSettings() }.nickname = nickname
                    }
                }

                if (freshMember) {
                    database.createMember(member)
                    for ((sid, settings) in memberServerSettings) {
                        database.createMemberServerSettings(MemberServerSettingsRecord().apply {
                            serverId = sid
                            systemId = system.id
                            memberId = member.id
                            avatarUrl = settings.avatar
                            nickname = settings.nickname
                        })
                    }
                } else {
                    database.updateMember(member)
                    for ((sid, settings) in memberServerSettings) {
                        val dbSettings = database.fetchMemberServerSettingsFromSystemAndMember(sid, system.id, member.id)
                        if (dbSettings == null) {
                            database.createMemberServerSettings(MemberServerSettingsRecord().apply {
                                serverId = sid
                                systemId = system.id
                                memberId = member.id
                                avatarUrl = settings.avatar
                                nickname = settings.nickname
                            })
                        } else {
                            dbSettings.avatarUrl = settings.avatar
                            dbSettings.nickname = settings.nickname
                            database.updateMemberServerSettings(dbSettings)
                        }
                    }
                }
                members += member
            }

            @Suppress("DEPRECATION")
            pkSystem.auto?.let {
                system.autoProxy = idMap[it]
                database.updateSystem(system)
            }
        }

        pkSystem.switches?.let { switches ->
            val existingSwitches = database.fetchSwitchesFromSystem(system.id)?.sortedBy { it.timestamp } ?: emptyList()
            val existingInstants = existingSwitches.mapTo(HashSet()) { it.timestamp }
            val existingIds = existingSwitches.mapTo(HashSet()) { it.id }
            id = existingIds.firstFreeRaw()
            val switchMap = TreeMap<Instant, LinkedHashSet<String>>(Instant::compareTo)
            for (switch in switches) {
                val timestamp = switch.timestamp.tryParseInstant() ?: continue
                switchMap.computeIfAbsent(timestamp) { LinkedHashSet() }.addAll(switch.members?.filterNotNull() ?: emptyList())
            }
            var lastMember: LinkedHashSet<String>? = null
            for ((timestamp, members) in switchMap) {
                if (lastMember eq members) continue
                lastMember = members
                if (timestamp in existingInstants) continue
                database.createSwitch(
                    SystemSwitchRecord(
                        systemId = system.id,
                        id = findNextId(existingIds),
                        memberIds = members.mapNotNull(idMap::get),
                        timestamp = timestamp,
                    )
                )
            }
        }
    }

    private infix fun <T> LinkedHashSet<T>?.eq(other: LinkedHashSet<T>?): Boolean {
        if (this === other) return true
        if (this == null || other == null || this.size != other.size) return false
        val ita = this.iterator()
        val itb = other.iterator()
        while (ita.hasNext() && itb.hasNext()) {
            if (ita.next() != itb.next()) return false
        }
        // Guards against concurrent modifications... if they somehow occur
        return !ita.hasNext() && !itb.hasNext()
    }

    private fun findBirthdays(members: Collection<PkMember>): Map<PkMember, LocalDate> {
        val preferMonthDay = shouldPreferMonthDay(system.timezone)
        val ambiguousFormat = if (preferMonthDay) MMDDuuuu else DDMMuuuu

        val birthdays = HashMap<PkMember, Pair<LocalDate, DateTimeFormatter>>()
        val ambiguousBirthdays = HashSet<PkMember>()

        // Parse out birthdays ahead of time
        for (pkMember in members) {
            pkMember.tryParseBirthday(preferMonthDay)?.let {
                birthdays[pkMember] = it
                if (it.second == ambiguousFormat && it.first.dayOfMonth <= 12) {
                    ambiguousBirthdays.add(pkMember)
                }
            }
        }

        // If there's any ambiguous birthdays, figure out the expected format.
        if (ambiguousBirthdays.isNotEmpty()) {
            val otherFormat = if (preferMonthDay) DDMMuuuu else MMDDuuuu
            var expectedCount = 0
            var otherCount = 0
            for ((pkMember, pair) in birthdays) {
                if (!ambiguousBirthdays.contains(pkMember)) {
                    if (pair.second == ambiguousFormat) expectedCount++
                    if (pair.second == otherFormat) otherCount++
                }
            }
            if (otherCount > expectedCount) {
                for (pkMember in ambiguousBirthdays) {
                    // Not null assertion as it was already parsed successfully once.
                    birthdays.computeIfPresent(pkMember) { _, (date, _) -> LocalDate.of(date.year, date.dayOfMonth, date.monthValue) to otherFormat }
                }
            }
        }

        return birthdays.mapValues { it.value.first }
    }

    private fun String?.validateId(map: HashMap<String?, String>, set: HashSet<String>): String {
        if (!isValidPkString() || this in set) {
            val newId = findNextId(set)
            map[this] = newId
            return newId
        }
        return this
    }

    private fun findNextId(set: HashSet<String>): String {
        var itr = 0
        var newId: String

        do {
            newId = id++.toPkString()
            if (itr++ > 100) throw ImporterException("Could not find free member ID")
        } while (newId in set)

        set += newId
        return newId
    }

    // Getters:
    final override lateinit var system: SystemRecord
        private set

    final override val members: ArrayList<MemberRecord> = ArrayList()

    override fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> = proxies[member]!!

    final override var createdMembers = 0
        private set

    final override var updatedMembers = 0
        private set

    private data class PfMemberServerSettings(
        var nickname: String? = null,
        var avatar: String? = null,
    )
}

