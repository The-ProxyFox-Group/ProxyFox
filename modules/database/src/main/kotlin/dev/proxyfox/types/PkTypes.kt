/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.types

import dev.proxyfox.common.fromColorForExport
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TrustLevel
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.gson.NullValueProcessor
import dev.proxyfox.gson.UnexpectedValueProcessor
import java.time.OffsetDateTime

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

    // Required for PK to accept the export.
    // ProxyFox however will accept any export that vaguely matches PK's.
    val switches: List<Any>? = emptyList(),
) {
    constructor(
        record: SystemRecord,
        members: List<PkMember>? = null,
        groups: List<PkGroup>? = null
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

    val proxy_tags: List<PkProxy>? = emptyList(),

    // Some data structures from here will need to be flattened in.
    val privacy: PkMemberPrivacy? = null,

    // ProxyFox-specific extensions.
    // PluralKit and TupperBox should ignore these.
    val proxyfox: PfMemberExtension? = null,
) {
    constructor(record: MemberRecord, proxyTags: List<PkProxy>?) : this(
        id = record.id,
        name = record.name,
        display_name = record.displayName,
        description = record.description,
        pronouns = record.pronouns,
        color = record.color.fromColorForExport(),
        keep_proxy = record.keepProxy,
        message_count = record.messageCount.toLong(),
        birthday = record.birthday?.toString(),
        created = record.timestamp.toString(),
        proxy_tags = proxyTags,
        avatar_url = record.avatarUrl,
        proxyfox = if (record.birthday != null || record.age != null || record.role != null) {
            PfMemberExtension(
                age = record.age,
                role = record.role,
            )
        } else null
    )
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
)

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
    val age: String?,
    val role: String?,
)

@Suppress("EnumEntryName")
enum class PkPrivacyEnum {
    public, private
}