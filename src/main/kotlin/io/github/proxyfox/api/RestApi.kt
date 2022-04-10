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
                // System settings
                get("systems/{user}") {

                }
                // Array of member IDs
                get("systems/{user}/members") {

                }
                // Member settings
                get("systems/{user}/member/{member}") {

                }
                // Member proxies
                get("systems/{user}/member/{member}/proxies") {

                }
                // System switches
                get("systems/{user}/switches") {

                }
            }
        }.start()
    }
}