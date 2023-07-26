/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.group

enum class TagMode {
    HIDDEN,
    BEFORE,
    AFTER
    ;

    fun getDisplayString(): String {
        return when (this) {
            HIDDEN -> "None"
            BEFORE -> "Before System"
            AFTER -> "After System"
        }
    }
}
