package dev.proxyfox.api.models

import dev.proxyfox.common.fromColor
import dev.proxyfox.database.records.member.MemberRecord
import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String,
    val name: String,
    val displayName: String?,
    val description: String?,
    val pronouns: String?,
    val color: String?,
    val avatarUrl: String?,
    val keepProxy: Boolean,
    val autoProxy: Boolean,
    val messageCount: ULong,
    val birthday: String?,
    val age: String?,
    val role: String?
) {
    companion object {
        fun fromRecord(member: MemberRecord) = Member(
            id = member.id,
            name = member.name,
            displayName = member.displayName,
            description = member.description,
            pronouns = member.pronouns,
            color = member.color.fromColor(),
            avatarUrl = member.avatarUrl,
            keepProxy = member.keepProxy,
            autoProxy = member.autoProxy,
            messageCount = member.messageCount,
            birthday = member.birthday.toString(),
            age = member.age,
            role = member.role
        )
    }
}