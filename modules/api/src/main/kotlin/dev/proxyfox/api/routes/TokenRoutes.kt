package dev.proxyfox.api.routes

import dev.proxyfox.api.models.Token
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tokenRoutes() {
    get("/tokens") {
        val tokenString = call.request.headers["Authorization"] ?: return@get call.respond("404: Not Found")
        val token = database.fetchToken(tokenString) ?: return@get call.respond("404: Not Found")
        call.respond(Token.fromRecord(token))
    }
}
