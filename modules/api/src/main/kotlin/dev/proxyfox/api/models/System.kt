/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.common.fromColor
import dev.proxyfox.database.PkId
import dev.proxyfox.database.etc.ktx.serializaton.InstantLongMillisecondSerializer
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.system.SystemRecord
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a system.
 *
 * Accessed via the `/systems/{sysid}` route.
 *
 * Requires a token to access.
 *
 * @param id the Pk-formatted ID of the system
 * @param name the name of the system
 * @param description the description of the system
 * @param tag the system tag
 * @param pronouns the pronouns for the system
 * @param color the default color for the system (in a hexadecimal RGB format)
 * @param avatarUrl the URL for the default avatar
 * @param timezone the timezone of the system
 * @param created the timestamp of the creation of the system
 * @param autoProxy the ID of the member that's currently being autoproxied
 * @param autoType the current mode of autoproxy
 * */
@Serializable
data class System(
    val id: PkId,
    val name: String?,
    val description: String?,
    val tag: String?,
    val pronouns: String?,
    val color: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    val timezone: String?,
    val created: Instant,
    @SerialName("auto_proxy")
    val autoProxy: String?,
    @SerialName("auto_type")
    val autoType: AutoProxyMode
) {
    companion object {
        fun fromRecord(system: SystemRecord) = System(
            id = system.id,
            name = system.name,
            description = system.description,
            tag = system.tag,
            pronouns = system.pronouns,
            color = system.color.fromColor(),
            avatarUrl = system.avatarUrl,
            timezone = system.timezone,
            created = system.timestamp,
            autoProxy = system.autoProxy,
            autoType = system.autoType
        )
    }
}