/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.kord.common.entity.Snowflake
import dev.proxyfox.api.models.Message
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.messageRoutes() {
    get("/messages/{id}") {
        val message = database.fetchMessage(Snowflake(call.parameters["id"]!!)) ?: return@get call.respond("Message not found")
        call.respond(Message.fromRecord(message))
    }
}