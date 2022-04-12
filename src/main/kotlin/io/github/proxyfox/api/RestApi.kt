package io.github.proxyfox.api

import io.github.proxyfox.printStep
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("REST API")

/**
 * Object for REST API related functions
 * @author Oliver
 * */
object RestApi {
    suspend fun start() {
        printStep("Start REST API server", 1)
        embeddedServer(Netty, 8080) {
            routing {
                route("/api/v1/systems") {
                    // Array of system IDs
                    get {
                        logger.info("Received GET for " + call.request.local.uri)
                        call.respondText("[]")
                        finish()
                    }
                    route("/{user}") {
                        // System settings
                        get {
                            logger.info("Received GET for " + call.request.local.uri)
                            val user = call.parameters["user"]
                            call.respondText("{}")
                            finish()
                        }
                        // Array of System switches
                        get("/switches") {
                            logger.info("Received GET for " + call.request.local.uri)
                            val user = call.parameters["user"]
                            call.respondText("[]")
                            finish()
                        }

                        route("/members") {
                            // Array of member IDs
                            get {
                                logger.info("Received GET for " + call.request.local.uri)
                                val user = call.parameters["user"]
                                call.respondText("[]")
                                finish()
                            }

                            route("/{member}") {
                                // Member settings
                                get {
                                    logger.info("Received GET for " + call.request.local.uri)
                                    val user = call.parameters["user"]
                                    val member = call.parameters["member"]
                                    call.respondText("{}")
                                    finish()
                                }
                                // Array of proxies
                                get("/proxies") {
                                    logger.info("Received GET for " + call.request.local.uri)
                                    val user = call.parameters["user"]
                                    val member = call.parameters["member"]
                                    call.respondText("[]")
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }.start()
    }
}