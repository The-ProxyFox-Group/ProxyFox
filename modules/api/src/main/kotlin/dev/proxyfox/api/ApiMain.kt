/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api

import dev.proxyfox.api.routes.memberRoutes
import dev.proxyfox.api.routes.messageRoutes
import dev.proxyfox.api.routes.switchRoutes
import dev.proxyfox.api.routes.systemRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*


object ApiMain {
    fun main() = embeddedServer(Netty, port = Integer.parseInt(System.getenv("PORT"))) {
        configureRouting()
        configureSerialization()
    }.start()

    private fun Application.configureSerialization() {
        install(ContentNegotiation) {
            json()
        }
    }

    private fun Application.configureRouting() {
        routing {
            systemRoutes()
            memberRoutes()
            switchRoutes()
            messageRoutes()
        }
    }
}