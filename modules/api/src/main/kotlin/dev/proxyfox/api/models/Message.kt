/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.kord.common.entity.Snowflake
import dev.proxyfox.common.snowflake
import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a proxied message.
 *
 * Accessed in the `/messages/{id}` route.
 *
 * Doesn't require a token.
 *
 * @param timestamp the time of creation
 * @param sender the Discord account ID of the author
 * @param original the message ID of the original message
 * @param proxied the message ID of the new (proxied) message
 * @param channel the ID of the channel the message was sent in
 * @param guild the ID of the guild the message was sent in
 * @param thread the ID of the thread the message was sent in, if applicable
 * @param system the Pk-formatted ID of the system that created the message
 * @param member the Pk-formatted ID of the member that created the message
 * */
@Serializable
data class Message(
    val timestamp: Instant,
    val sender: Snowflake,
    val original: Snowflake,
    val proxied: Snowflake,
    val channel: Snowflake,
    val guild: Snowflake,
    val thread: Snowflake?,
    val system: PkId,
    val member: PkId
) {
    companion object {
        fun fromRecord(record: ProxiedMessageRecord) = Message(
            timestamp = record.creationDate,
            sender = record.userId.snowflake,
            original = record.oldMessageId.snowflake,
            proxied = record.newMessageId.snowflake,
            channel = record.channelId.snowflake,
            guild = record.guildId.snowflake,
            thread = record.threadId?.snowflake,
            system = record.systemId,
            member = record.memberId
        )
    }
}