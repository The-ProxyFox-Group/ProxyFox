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
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.types.PkMember
import dev.proxyfox.types.PkSystem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private lateinit var system: SystemRecord
    private var members: List<MemberRecord> = ArrayList()
    private var proxies: HashMap<MemberRecord, List<MemberProxyTagRecord>> = HashMap()
    private var createdMembers = 0
    private var updatedMembers = 0
    private var id = 0

    constructor() : this(false, false)

    override suspend fun import(database: Database, json: JsonObject, userId: ULong) {
        val pkSystem = gson.fromJson(json, PkSystem::class.java)
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

        if (pkSystem.members != null) {
            val idMap = HashMap<String?, String>()
            val birthdays = findBirthdays(pkSystem.members)

            for (pkMember in pkSystem.members) {
                val member = if (fresh) {
                    createdMembers++
                    MemberRecord(
                        if (directAllocation) pkMember.id.validateId(idMap) else nextId(),
                        system.id,
                        pkMember.name.validate("members/name"),
                    )
                } else database.fetchMemberFromSystemAndName(system.id, pkMember.name.validate("members/name"))?.apply { updatedMembers++ }
                    ?: database.getOrCreateMember(system.id, pkMember.name.validate("members/name"), id = if (directAllocation) pkMember.id else null)?.apply { createdMembers++ }
                    ?: throw ImporterException("Database didn't create member")

                member.displayName = pkMember.display_name.sanitise() ?: member.displayName
                member.avatarUrl = pkMember.avatar_url.sanitise() ?: member.avatarUrl
                member.description = pkMember.description.sanitise() ?: member.description
                member.pronouns = pkMember.pronouns.sanitise() ?: member.pronouns
                try {
                    member.color = pkMember.color?.validate("members[${pkMember.id} (${pkMember.name})]/color")?.toColor() ?: member.color
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

                pkMember.proxy_tags?.forEach { pkProxy ->
                    database.createProxyTag(system.id, member.id, pkProxy.prefix, pkProxy.suffix)
                }
                if (directAllocation) {
                    pkMember.created.tryParseOffsetTimestamp()?.let { member.timestamp = it }
                }
                database.updateMember(member)
            }
        }
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

    private fun String?.validateId(set: HashMap<String?, String>): String {
        if (!isValidPkString() || this in set) {
            var itr = 0
            var newId: String
            do {
                newId = nextId()
                if (itr++ > 100) throw ImporterException("Could not find free member ID")
            } while (newId in set)
            set[this] = newId
            return newId
        }
        return this
    }

    private fun nextId() = id++.toPkString()

    // Getters:
    override suspend fun getSystem(): SystemRecord = system

    override suspend fun getMembers(): List<MemberRecord> = members

    override suspend fun getMemberProxyTags(member: MemberRecord): List<MemberProxyTagRecord> = proxies[member]!!

    override suspend fun getNewMembers(): Int = createdMembers

    override suspend fun getUpdatedMembers(): Int = updatedMembers
}

