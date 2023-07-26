/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.proxyfox.api.models.ApiError
import dev.proxyfox.api.models.Token
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tokenRoutes() {
    get("/tokens") {
        val tokenString = call.request.headers["Authorization"] ?: return@get call.respond(ApiError(404, "Not Found"))
        val token = database.fetchToken(tokenString) ?: return@get call.respond(ApiError(404, "Not Found"))
        call.respond(Token.fromRecord(token))
    }
}
