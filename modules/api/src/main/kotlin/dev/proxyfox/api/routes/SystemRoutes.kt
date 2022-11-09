/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.proxyfox.api.models.*
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.systemRoutes() {
    route("/systems/{id}") {
        get {
            val system = database.fetchSystemFromId(call.parameters["id"]!!) ?: return@get call.respond("System not found")
            call.respond(System(
                id = system.id,
                name = system.name,
                description = system.description,
                tag = system.tag,
                pronouns = system.pronouns,
                color = Integer.toHexString(system.color),
                avatarUrl = system.avatarUrl,
                timezone = system.timezone,
                timestamp = system.timestamp.toString()
            ))
        }
    }
}