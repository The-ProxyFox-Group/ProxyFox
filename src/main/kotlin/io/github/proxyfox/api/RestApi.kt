package io.github.proxyfox.api

import io.github.proxyfox.printStep
import io.ktor.application.*
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
                route("/systems") {
                    // Array of system IDs
                    get {

                    }
                    route("/{user}") {
                        // System settings
                        get {
                            val user = call.parameters["user"]
                        }
                        // Array of System switches
                        get("/switches") {
                            val user = call.parameters["user"]
                        }

                        route("/members") {
                            // Array of member IDs
                            get {
                                val user = call.parameters["user"]
                            }

                            route("/{member}") {
                                // Member settings
                                get {
                                    val user = call.parameters["user"]
                                    val member = call.parameters["member"]
                                }
                                // Array of proxies
                                get("/proxies") {
                                    val user = call.parameters["user"]
                                    val member = call.parameters["member"]
                                }
                            }
                        }
                    }
                }
            }
        }.start()
    }
}