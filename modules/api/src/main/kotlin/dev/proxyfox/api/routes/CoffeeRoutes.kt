/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.proxyfox.api.models.ApiError
import dev.proxyfox.api.models.BrewState
import dev.proxyfox.api.models.Coffee
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

var coffees = HashMap<String, Coffee>()

fun generateIdString(): String {
    val chars = 'A'..'Z'
    var str = "";
    for (i in 0..5) {
        str += chars.random()
    }

    return str
}

fun Route.coffeeRoutes() {
    route("/coffee") {
        method(CoffeeMethods.Brew) { handle {
            if (Math.random() < 0.0625) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@handle
            }

            val id = generateIdString()

            if (coffees.containsKey(id)) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@handle
            }

            val coffee = Coffee(
                id,
                BrewState.STARTING
            )

            coffees[id] = coffee

            call.respond(HttpStatusCode.OK, coffee)
        }}

        get("/{coffee}") {
            val id = call.parameters["coffee"]

            if (id == null) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@get
            }

            val coffee = coffees[id]

            if (coffee == null) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@get
            }

            coffee.state = coffee.state.advance()

            call.respond(HttpStatusCode.OK, coffee)
        }

        route("/{coffee}", CoffeeMethods.When) { handle {
            val id = call.parameters["coffee"]

            if (id == null) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@handle
            }

            val coffee = coffees[id]

            if (coffee == null) {
                call.respond(CoffeeCodes.Teapot, ApiError(418, "I'm a teapot!"))
                return@handle
            }

            call.respond(HttpStatusCode.OK, coffee)
            coffees.remove(id)
        }}
    }
}

object CoffeeMethods {
    val Brew = HttpMethod("BREW")
    val When = HttpMethod("WHEN")
}

object CoffeeCodes {
    val Teapot = HttpStatusCode(418, "I'm a Teapot!")
}
