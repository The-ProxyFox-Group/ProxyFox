/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.types

class PkSystem {
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var tag: String? = null
    var avatar_url: String? = null
    var members: Array<PkMember>? = arrayOf()

    // Required for PK to accept the export.
    var switches: Array<Any> = arrayOf()
}

class PkMember {
    var id: String? = null
    var name: String = ""
    var display_name: String? = null
    var description: String? = null
    var pronouns: String? = null
    var color: String? = null
    var keep_proxy: Boolean? = false
    var message_count: ULong? = 0UL
    var proxy_tags: Array<PkProxy>? = arrayOf()
    var avatar_url: String? = null
}

class PkProxy {
    var prefix: String? = null
    var suffix: String? = null
}