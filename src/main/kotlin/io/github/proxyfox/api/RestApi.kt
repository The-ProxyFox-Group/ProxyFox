package io.github.proxyfox.api

import io.github.proxyfox.printStep
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

private val logger = LoggerFactory.getLogger("REST API")

object RestApi {
    suspend fun start() {
        printStep("Start REST API server", 1)
        embeddedServer(Netty, 8080) {
            routing {
                get {
                    call.respond(HttpStatusCode.NotFound)
                    finish()
                }
            }
        }.start()
    }
}