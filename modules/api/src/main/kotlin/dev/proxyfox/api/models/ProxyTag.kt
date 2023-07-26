/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.models

import dev.proxyfox.database.records.member.MemberProxyTagRecord
import kotlinx.serialization.Serializable

/**
 * Represents a proxy tag.
 *
 * @param prefix the prefix for the proxy
 * @param suffix the suffix for the proxy
 * */
@Serializable
data class ProxyTag(val prefix: String?, val suffix: String?) {
    companion object {
        fun fromRecord(record: MemberProxyTagRecord) = ProxyTag(record.prefix, record.suffix)
    }
}