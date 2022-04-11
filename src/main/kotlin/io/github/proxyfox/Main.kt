@file:JvmName("Main")

package io.github.proxyfox

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.proxyfox.api.RestApi
import io.github.proxyfox.command.CommandSource
import io.github.proxyfox.command.Commands
import io.github.proxyfox.command.dispatcher
import io.github.proxyfox.command.runAsync
import io.github.proxyfox.database.Database
import io.github.proxyfox.database.NopDatabase
import io.github.proxyfox.webhook.WebhookUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("ProxyFox")

suspend fun printFancy(input: String) {
    val edges = "*".repeat(input.length + 4)
    logger.info(edges)
    logger.info("* $input *")
    logger.info(edges)
}

suspend fun printStep(input: String, step: Int) {
    val add = "  ".repeat(step)
    logger.info(step.toString() + add + input)
}

val prefixRegex = Regex("^pf[>;!].*",RegexOption.IGNORE_CASE)

lateinit var kord: Kord
lateinit var database: Database

suspend fun main() {
    // Hack to not get io.ktor.random warning
    System.setProperty("io.ktor.random.secure.random.provider", "DRBG")

    printFancy("Initializing ProxyFox")

    // Register commands in brigadier
    Commands.register()

    // Setup database
    setupDatabase()

    // Start reading console input
    readConsole()

    // Start REST API
    RestApi.start()

    // Login to Kord
    login()
}

suspend fun setupDatabase() {
    printStep("Setup database", 1)
    database = NopDatabase()
}

suspend fun readConsole() {
    printStep("Start reading console input", 1)
    printStep("Launching thread", 2)
    thread {
        runAsync {
            while (true) {
                val input = readln()
                if (input.contains("stop"))
                    exitProcess(0)
            }
        }
    }
}
@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Logging in",1)
    kord = Kord(System.getenv("PROXYFOX_KEY"))

    // Register events
    printStep("Registering events", 2)
    kord.on<MessageCreateEvent> {
        // Return if bot
        if (message.webhookId != null || message.author!!.isBot) return@on

        // Get message content to check with regex
        val content = message.content
        if (prefixRegex.matches(content)) {
            // Remove the prefix to pass into dispatcher
            val contentWithoutRegex = content.substring(3)
            dispatcher.execute(contentWithoutRegex,CommandSource(message))
        } else {
            // Proxy the message
            val proxy = database.getProxyTagFromMessage(message.author!!.id,content)
            if (proxy != null) {
                val member = database.getMemberById(proxy.systemId,proxy.memberId)!!
                WebhookUtil.prepareMessage(message, member, proxy).send()
            }
        }
    }
    kord.on<ReadyEvent> {
        printFancy("ProxyFox initialized")
        launch {
            updatePresence()
        }
    }

    // Login
    printStep("Finalize login", 2)
    kord.login {
        intents += Intent.Guilds
        intents += Intent.GuildMessages
        intents += Intent.GuildMessageReactions
        intents += Intent.GuildWebhooks
        intents += Intent.DirectMessages
        intents += Intent.DirectMessages
        intents += Intent.DirectMessagesReactions
        intents += Intent.MessageContent
    }
}

suspend fun updatePresence() {
    while (true) {
        kord.editPresence {
            val servers = kord.guilds.count()
            playing("Run pf>help for help! in $servers servers!")
        }
        delay(30000)
    }
}