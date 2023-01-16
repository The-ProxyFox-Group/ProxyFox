/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api

import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*

val AuthenticationPlugin = createRouteScopedPlugin(name = "AuthenticationPlugin") {
    onCall { call ->
        val token = call.request.headers["Authorization"] ?: return@onCall call.respond("401 Unauthorized")
        database.fetchToken(token) ?: return@onCall call.respond("401 Unauthorized")
    }
}
