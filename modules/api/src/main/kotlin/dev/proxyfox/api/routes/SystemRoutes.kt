/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.kord.common.entity.Snowflake
import dev.proxyfox.api.AuthenticationPlugin
import dev.proxyfox.api.models.System
import dev.proxyfox.api.models.SystemGuildSettings
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.systemRoutes() {
    route("/systems/{id}") {
        install(AuthenticationPlugin)
        get {
            val system = database.fetchSystemFromId(call.parameters["id"]!!) ?: return@get call.respond("System not found")
            call.respond(System.fromRecord(system))
        }

        get("/guilds/{guild}") {
            val id = call.parameters["id"] ?: return@get call.respond("System not found")
            val settings = database.getOrCreateServerSettingsFromSystem(Snowflake(call.parameters["guild"]!!).value, id)
            call.respond(SystemGuildSettings.fromRecord(settings))
        }
    }
}