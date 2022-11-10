/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.proxyfox.api.AuthenticationPlugin
import dev.proxyfox.api.models.Member
import dev.proxyfox.api.models.Switch
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.switchRoutes() {
    route("/systems/{id}/switches") {
        install(AuthenticationPlugin)
        get {
            val id = call.parameters["id"] ?: return@get call.respond("System not found")
            call.respond(database.fetchSwitchesFromSystem(id)?.map(Switch.Companion::fromRecord) ?: emptyList())
        }
    }

    route("/systems/{id}/fronters") {
        get {
            val id = call.parameters["id"] ?: return@get call.respond("System not found")
            call.respond(database.fetchFrontingMembersFromSystem(id)?.map(Member.Companion::fromRecord) ?: emptyList())
        }
    }
}