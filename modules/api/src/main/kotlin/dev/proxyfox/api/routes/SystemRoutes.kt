/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.kord.common.entity.Snowflake
import dev.proxyfox.api.getAccess
import dev.proxyfox.api.models.ApiError
import dev.proxyfox.api.models.System
import dev.proxyfox.api.models.SystemGuildSettings
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.systemRoutes() {
    route("/systems/{id}") {
        getAccess {
            val system = database.fetchSystemFromId(call.parameters["id"]!!)
                    ?: return@getAccess call.respond(ApiError(404, "SystemNot Found"))
            call.respond(System.fromRecord(system))
        }

        getAccess("/guilds/{guild}") {
            val id = call.parameters["id"] ?: return@getAccess call.respond(ApiError(404, "System Not Found"))
            val settings = database.getOrCreateServerSettingsFromSystem(Snowflake(call.parameters["guild"]!!).value, id)
            call.respond(SystemGuildSettings.fromRecord(settings))
        }
    }
}
