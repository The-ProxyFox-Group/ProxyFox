/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api

import dev.proxyfox.api.models.ApiError
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.TokenType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

@Suppress("FunctionName")
fun ApiPlugin(name: String, accessFunction: TokenType.() -> Boolean) = createRouteScopedPlugin(name = name) {
    onCall { call ->
        val tokenString = call.request.headers["Authorization"]
                ?: return@onCall call.respond(ApiError(401, "Unauthorized"))
        val token = database.fetchToken(tokenString) ?: return@onCall call.respond(ApiError(401, "Unauthorized"))
        if (token.type.accessFunction()) return@onCall call.respond(ApiError(401, "Unauthorized"))
    }
}

val AccessPlugin = ApiPlugin("AccessPlugin", TokenType::canViewApi)

val EditPlugin = ApiPlugin("EditPlugin", TokenType::canEditApi)

@KtorDsl
fun Route.getAccess(body: PipelineInterceptor<Unit, ApplicationCall>) {
    install(AccessPlugin)
    get(body)
}

@KtorDsl
fun Route.getAccess(path: String, body: PipelineInterceptor<Unit, ApplicationCall>) {
    route(path, HttpMethod.Get) {
        install(AccessPlugin)
        handle(body)
    }
}
