package io.github.proxyfox.api

import io.github.proxyfox.printStep
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("REST API")

object RestApi {
    /**
     * @author Oliver
     * */
    suspend fun start() {
        printStep("Start REST API server", 1)
        embeddedServer(Netty, 8080) {
            routing {
                // Array of system IDs
                get("/systems") {

                }
                // System settings
                get("/systems/{user}") {

                }
                // Array of member IDs
                get("/systems/{user}/members") {

                }
                // Member settings
                get("/systems/{user}/members/{member}") {

                }
                // Array Member proxies
                get("/systems/{user}/members/{member}/proxies") {

                }
                // Array of System switches
                get("/systems/{user}/switches") {

                }
            }
        }.start()
    }
}