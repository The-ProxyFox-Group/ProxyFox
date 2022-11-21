/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.types

import dev.proxyfox.common.fromColorForExport
import dev.proxyfox.database.*
import dev.proxyfox.database.pkValid
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.format.DateTimeFormatter

@JvmRecord
@Serializable
data class PkSystem(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val tag: String? = null,
    val pronouns: String? = null,
    val avatar_url: String? = null,
    val banner: String? = null,
    val color: String? = null,
    val created: String? = null,
    val webhook_url: String? = null,

    val privacy: PkSystemPrivacy? = null,
    val config: PkConfig? = null,

    val accounts: List<ULong>? = null,
    val members: List<PkMember>? = null,
    val groups: List<PkGroup>? = null,

    // ProxyFox-specific extensions.
    // PluralKit and TupperBox should ignore these.
    val proxyfox: PfSystemExtension? = null,

    // Required for PFv1 database imports
    @Deprecated("PFv1 database imports only")
    @SerialName("auto_bool")
    val autoBool: Boolean? = null,

    @Deprecated("PFv1 database imports only")
    val auto: String? = null,

    @Deprecated("PFv1 database imports only")
    @SerialName("server_proxy")
    val serverProxyEnabled: Map<ULong, Boolean>? = null,

    // Ignored
    @Deprecated("PFv1 database imports only")
    @Transient
    val subsystems: Unit? = null,

    // Required for PK to accept the export.
    // ProxyFox however will accept any export that vaguely matches PK's.
    val switches: List<PkSwitch>? = emptyList(),

    // The following are ignored for as we don't support these yet,
    // at least at this location.
    @Transient
    val tz: Unit? = null,
    @Transient
    val timezone: Unit? = null,

    @Transient
    val description_privacy: Unit? = null,
    @Transient
    val pronoun_privacy: Unit? = null,
    @Transient
    val member_list_privacy: Unit? = null,
    @Transient
    val group_list_privacy: Unit? = null,
    @Transient
    val front_privacy: Unit? = null,
    @Transient
    val front_history_privacy: Unit? = null,

    // The following are ignored entirely. We don't use these.
    @Transient
    val uuid: Unit? = null,
    @Transient
    val version: Unit? = null,
) {
    constructor(
        record: SystemRecord,
        members: List<PkMember>? = null,
        groups: List<PkGroup>? = null,
        switches: List<PkSwitch>? = null,
    ) : this(
        id = record.id,
        accounts = record.users,
        name = record.name,
        description = record.description,
        tag = record.tag,
        pronouns = record.pronouns,
        color = record.color.fromColorForExport(),
        avatar_url = record.avatarUrl,
        created = record.timestamp.toString(),
        config = PkConfig(
            timezone = record.timezone,
        ),
        members = members,
        groups = groups,
        // A list is required for a PK-compatible export.
        switches = switches ?: emptyList(),
        proxyfox = PfSystemExtension(
            trust = record.trust,
            autoType = record.autoType,
            autoProxy = record.autoProxy,
        )
    )
}

@JvmRecord
@Serializable
data class PkMember(
    val id: String? = null,
    val name: String? = null,
    val display_name: String? = null,
    val description: String? = null,
    val pronouns: String? = null,
    val color: String? = null,
    val keep_proxy: Boolean? = false,
    val message_count: Long? = 0L,

    // Note: Exporting is *strictly* ISO_LOCAL_DATE.
    // Importing will be much more lenient due to PFv1 not enforcing proper constraints on birthdays.
    val birthday: String? = null,

    // Note: Exporting is *strictly* ISO_OFFSET_DATE_TIME.
    // Importing will be much more lenient due to PFv1 not enforcing proper constraints on imports.
    // PF will ignore this unless this is a direct allocation.
    val created: String? = null,

    // The following two will need to be sanitised to valid HTTP/HTTPS URIs.
    val avatar_url: String? = null,
    val banner: String? = null,

    val proxy_tags: Set<PkProxy>? = emptySet(),

    // Some data structures from here will need to be flattened in.
    val privacy: PkMemberPrivacy? = null,

    // ProxyFox-specific extensions.
    // PluralKit and TupperBox should ignore these.
    val proxyfox: PfMemberExtension? = null,

    // Required for PFv1 database imports
    @Deprecated("PFv1 database imports only")
    @SerialName("server_nick")
    val serverNicknames: Map<ULong, String?>? = null,

    @Deprecated("PFv1 database imports only")
    @SerialName("server_avatar")
    val serverAvatars: Map<ULong, String?>? = null,

    // The following are ignored for as we don't support these yet,
    // at least at this location.
    @Transient
    val visibility: Unit? = null,
    @Transient
    val name_privacy: Unit? = null,
    @Transient
    val description_privacy: Unit? = null,
    @Transient
    val birthday_privacy: Unit? = null,
    @Transient
    val pronoun_privacy: Unit? = null,
    @Transient
    val avatar_privacy: Unit? = null,
    @Transient
    val metadata_privacy: Unit? = null,

    // The following are ignored. We don't use these.
    @Transient
    val uuid: Unit? = null,
) {
    constructor(record: MemberRecord, proxyTags: Set<PkProxy>?) : this(
        id = record.id,
        name = record.name,
        display_name = record.displayName,
        description = record.description,
        pronouns = record.pronouns,
        color = record.color.fromColorForExport(),
        keep_proxy = record.keepProxy,
        message_count = record.messageCount.toLong(),
        birthday = record.birthday?.run { if (pkValid()) toString() else "0004-${monthNumber.paddedString(2)}-${dayOfMonth.paddedString(2)}" },
        created = record.timestamp.toString(),
        proxy_tags = proxyTags,
        avatar_url = record.avatarUrl,
        proxyfox = PfMemberExtension(
            birthday = record.birthday.run { if (!pkValid()) toString() else null },
            age = record.age,
            role = record.role,
            autoProxy = record.autoProxy,
        )
    )

    fun tryParseBirthday(preferMonthDay: Boolean): Pair<LocalDate, DateTimeFormatter>? {
        val let = { it: String -> tryParseLocalDate(it, preferMonthDay) }
        return proxyfox?.birthday?.let(let) ?: birthday?.let(let)
    }
}

@JvmRecord
@Serializable
data class PkGroup(
    val id: String? = null,
    val name: String? = null,
    val display_name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val banner: String? = null,
    val color: String? = null,
    val created: Instant? = null,
    val members: List<String>? = null,

    val privacy: PkGroupPrivacy? = null,

    // The following are ignored. We don't use these.
    @Transient
    val uuid: Unit? = null,
)

@JvmRecord
@Serializable
data class PkSwitch(
    val timestamp: String? = null,
    val members: List<String?>? = null,

    // Ignored for PFv1 database imports
    @Deprecated("PFv1 database imports only")
    @Transient
    val id: Unit? = null,
) {
    constructor(record: SystemSwitchRecord) : this(
        timestamp = record.timestamp.toString(),
        members = record.memberIds,
    )
}

@JvmRecord
@Serializable
data class PkProxy(
    val prefix: String? = null,
    val suffix: String? = null
) {
    constructor(record: MemberProxyTagRecord) : this(
        prefix = record.prefix,
        suffix = record.suffix
    )
}

@JvmRecord
@Serializable
data class PkSystemPrivacy(
    val description_privacy: PkPrivacyEnum? = null,
    val pronoun_privacy: PkPrivacyEnum? = null,
    val member_list_privacy: PkPrivacyEnum? = null,
    val group_list_privacy: PkPrivacyEnum? = null,
    val front_privacy: PkPrivacyEnum? = null,
    val front_history_privacy: PkPrivacyEnum? = null,
) {
    constructor(privacy: PkPrivacyEnum) : this(
        description_privacy = privacy,
        pronoun_privacy = privacy,
        member_list_privacy = privacy,
        group_list_privacy = privacy,
        front_privacy = privacy,
        front_history_privacy = privacy,
    )
}

@JvmRecord
@Serializable
data class PkMemberPrivacy(
    val visibility: PkPrivacyEnum? = null,
    val name_privacy: PkPrivacyEnum? = null,
    val description_privacy: PkPrivacyEnum? = null,
    val birthday_privacy: PkPrivacyEnum? = null,
    val pronoun_privacy: PkPrivacyEnum? = null,
    val avatar_privacy: PkPrivacyEnum? = null,
    val metadata_privacy: PkPrivacyEnum? = null,
) {
    constructor(privacy: PkPrivacyEnum) : this(
        visibility = privacy,
        name_privacy = privacy,
        description_privacy = privacy,
        birthday_privacy = privacy,
        pronoun_privacy = privacy,
        avatar_privacy = privacy,
        metadata_privacy = privacy,
    )
}

@JvmRecord
@Serializable
data class PkGroupPrivacy(
    val name_privacy: PkPrivacyEnum? = null,
    val description_privacy: PkPrivacyEnum? = null,
    val icon_privacy: PkPrivacyEnum? = null,
    val list_privacy: PkPrivacyEnum? = null,
    val metadata_privacy: PkPrivacyEnum? = null,
    val visibility: PkPrivacyEnum? = null,
) {
    constructor(privacy: PkPrivacyEnum) : this(
        name_privacy = privacy,
        description_privacy = privacy,
        icon_privacy = privacy,
        list_privacy = privacy,
        metadata_privacy = privacy,
        visibility = privacy,
    )
}

@JvmRecord
@Serializable
data class PkConfig(
    val timezone: String? = null,
    val pings_enabled: Boolean? = true,
    val latch_timeout: Int? = null,
    val member_default_private: Boolean? = true,
    val group_default_private: Boolean? = true,
    val show_private_info: Boolean? = false,
    val member_limit: Int? = 1000,
    val group_limit: Int? = 250,
    val description_templates: List<String>? = null,
)

@JvmRecord
@Serializable
data class PfSystemExtension(
    val trust: Map<ULong, TrustLevel>? = null,
    val autoType: AutoProxyMode? = null,
    val autoProxy: String? = null,
)

@JvmRecord
@Serializable
data class PfMemberExtension(
    val birthday: String? = null,
    val age: String? = null,
    val role: String? = null,
    val autoProxy: Boolean? = null,
)

@Suppress("EnumEntryName")
enum class PkPrivacyEnum {
    public, private
}