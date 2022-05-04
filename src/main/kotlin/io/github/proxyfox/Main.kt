@file:JvmName("Main")

package io.github.proxyfox

import io.github.proxyfox.api.RestApi
import io.github.proxyfox.string.node.LiteralNode
import io.github.proxyfox.string.node.StringNode
import io.github.proxyfox.string.parser.nodes
import io.github.proxyfox.terminal.TerminalCommands


/**
 * @author Oliver
 * */
suspend fun main() {


    // Hack to not get io.ktor.random warning
    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")

    printFancy("Initializing ProxyFox")

//    // Register commands in brigadier
//    Commands.register()
    val node = LiteralNode("test") {
        "test!"
    }
    node.addSubNode(StringNode("test2") {
        params["test2"]!!
    })
    nodes.add(node)

    // Setup database
    setupDatabase()

    // Start reading console input
    TerminalCommands.start()

    // Start REST API
    RestApi.start()

    // Login to Kord
    login()
}