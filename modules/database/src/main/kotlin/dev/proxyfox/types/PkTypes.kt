/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.types

import com.google.gson.annotations.SerializedName
import dev.proxyfox.common.fromColorForExport
import dev.proxyfox.database.paddedString
import dev.proxyfox.database.pkCompatibleIso8601
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import dev.proxyfox.database.tryParseLocalDate
import dev.proxyfox.gson.NullValueProcessor
import dev.proxyfox.gson.UnexpectedValueProcessor
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@JvmRecord
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
    @SerializedName("auto_bool")
    val autoBool: Boolean? = null,

    @Deprecated("PFv1 database imports only")
    val auto: String? = null,

    @Deprecated("PFv1 database imports only")
    @SerializedName("server_proxy")
    val serverProxyEnabled: Map<ULong, Boolean>? = null,

    // Ignored
    @Deprecated("PFv1 database imports only")
    val subsystems: Void? = null,

    // Required for PK to accept the export.
    // ProxyFox however will accept any export that vaguely matches PK's.
    val switches: List<PkSwitch>? = emptyList(),

    // The following are ignored for as we don't support these yet,
    // at least at this location.
    val tz: Void? = null,
    val timezone: Void? = null,

    val description_privacy: Void? = null,
    val pronoun_privacy: Void? = null,
    val member_list_privacy: Void? = null,
    val group_list_privacy: Void? = null,
    val front_privacy: Void? = null,
    val front_history_privacy: Void? = null,

    // The following are ignored entirely. We don't use these.
    val uuid: Void? = null,
    val version: Void? = null,
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
        created = record.timestamp.pkCompatibleIso8601(),
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
    @SerializedName("server_nick")
    val serverNicknames: Map<ULong, String?>? = null,

    @Deprecated("PFv1 database imports only")
    @SerializedName("server_avatar")
    val serverAvatars: Map<ULong, String?>? = null,

    // The following are ignored for as we don't support these yet,
    // at least at this location.
    val visibility: Void? = null,
    val name_privacy: Void? = null,
    val description_privacy: Void? = null,
    val birthday_privacy: Void? = null,
    val pronoun_privacy: Void? = null,
    val avatar_privacy: Void? = null,
    val metadata_privacy: Void? = null,

    // The following are ignored. We don't use these.
    val uuid: Void? = null,
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
        birthday = record.birthday?.let { if (it.year in 1..9999) it.toString() else "0001-${it.monthValue.paddedString(2)}-${it.dayOfMonth.paddedString(2)}" },
        created = record.timestamp.pkCompatibleIso8601(),
        proxy_tags = proxyTags,
        avatar_url = record.avatarUrl,
        proxyfox = if (record.birthday != null || record.age != null || record.role != null || !record.autoProxy) {
            PfMemberExtension(
                birthday = record.birthday?.toString(),
                age = record.age,
                role = record.role,
                autoProxy = record.autoProxy,
            )
        } else null
    )

    fun tryParseBirthday(preferMonthDay: Boolean): Pair<LocalDate, DateTimeFormatter>? {
        val let = { it: String -> tryParseLocalDate(it, preferMonthDay) }
        return proxyfox?.birthday?.let(let) ?: birthday?.let(let)
    }
}

@JvmRecord
data class PkGroup(
    val id: String? = null,
    val name: String? = null,
    val display_name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val banner: String? = null,
    val color: String? = null,
    val created: OffsetDateTime? = null,
    val members: List<String>? = null,

    val privacy: PkGroupPrivacy? = null,

    // The following are ignored. We don't use these.
    val uuid: Void? = null,
)

@JvmRecord
data class PkSwitch(
    val timestamp: String?,
    val members: List<String?>?,

    // Ignored for PFv1 database imports
    @Deprecated("PFv1 database imports only")
    val id: Void? = null,
) {
    constructor(record: SystemSwitchRecord) : this(
        timestamp = record.timestamp.pkCompatibleIso8601(),
        members = record.memberIds,
    )
}

@JvmRecord
data class PkProxy(
    val prefix: String?,
    val suffix: String?
) {
    constructor(record: MemberProxyTagRecord) : this(
        prefix = record.prefix,
        suffix = record.suffix
    )
}

@UnexpectedValueProcessor<Any?>(NullValueProcessor::class)
@JvmRecord
data class PkSystemPrivacy(
    val description_privacy: PkPrivacyEnum?,
    val pronoun_privacy: PkPrivacyEnum?,
    val member_list_privacy: PkPrivacyEnum?,
    val group_list_privacy: PkPrivacyEnum?,
    val front_privacy: PkPrivacyEnum?,
    val front_history_privacy: PkPrivacyEnum?,
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

@UnexpectedValueProcessor<Any?>(NullValueProcessor::class)
@JvmRecord
data class PkMemberPrivacy(
    val visibility: PkPrivacyEnum?,
    val name_privacy: PkPrivacyEnum?,
    val description_privacy: PkPrivacyEnum?,
    val birthday_privacy: PkPrivacyEnum?,
    val pronoun_privacy: PkPrivacyEnum?,
    val avatar_privacy: PkPrivacyEnum?,
    val metadata_privacy: PkPrivacyEnum?,
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

@UnexpectedValueProcessor<Any?>(NullValueProcessor::class)
@JvmRecord
data class PkGroupPrivacy(
    val name_privacy: PkPrivacyEnum?,
    val description_privacy: PkPrivacyEnum?,
    val icon_privacy: PkPrivacyEnum?,
    val list_privacy: PkPrivacyEnum?,
    val metadata_privacy: PkPrivacyEnum?,
    val visibility: PkPrivacyEnum?,
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
data class PfSystemExtension(
    val trust: Map<ULong, TrustLevel>?,
    val autoType: AutoProxyMode?,
    val autoProxy: String?,
)

@JvmRecord
data class PfMemberExtension(
    val birthday: String?,
    val age: String?,
    val role: String?,
    val autoProxy: Boolean?,
)

@Suppress("EnumEntryName")
enum class PkPrivacyEnum {
    public, private
}