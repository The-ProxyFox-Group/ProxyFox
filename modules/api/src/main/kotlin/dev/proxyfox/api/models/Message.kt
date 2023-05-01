/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import kotlinx.serialization.Serializable

@Serializable
data class Message(
        val timestamp: String,
        val sender: String,
        val original: String,
        val proxied: String,
        val channel: String,
        val guild: String,
        val thread: String?,
        val system: String,
        val member: String
) {
    companion object {
        fun fromRecord(record: ProxiedMessageRecord) = Message(
            timestamp = record.creationDate.toString(),
            sender = record.userId.toString(),
            original = record.oldMessageId.toString(),
            proxied = record.newMessageId.toString(),
            channel = record.channelId.toString(),
            guild = record.guildId.toString(),
            thread = record.threadId.toString(),
            system = record.systemId,
            member = record.memberId
        )
    }
}