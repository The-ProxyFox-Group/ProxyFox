/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.sync

import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.pluralkt.PluralKt
import dev.proxyfox.pluralkt.types.PkSystem

object PkSync {
    fun pull(token: String, system: SystemRecord) {
        PluralKt.System.getMe(token) {
            if (isSuccess()) {
                getSuccess().getMembers(token)
            }
        }
    }

    private fun PkSystem.getMembers(token: String) {
        PluralKt.Member.getMembers(id, token) {
            if (isSuccess()) {
                for (member in getSuccess()) {
                    //...
                }
            }
        }
    }
}