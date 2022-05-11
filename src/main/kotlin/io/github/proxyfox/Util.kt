package io.github.proxyfox

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.proxyfox.database.Database
import io.github.proxyfox.database.NopDatabase
import io.github.proxyfox.database.PostgresDatabase
import io.github.proxyfox.string.parser.parseString
import io.github.proxyfox.webhook.WebhookUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.postgresql.Driver
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Important functions and variables needed for proxyfox
 * @author Oliver
 * */

val logger = LoggerFactory.getLogger("ProxyFox")
val prefixRegex = Regex("^pf[>;!].*", RegexOption.IGNORE_CASE)
lateinit var kord: Kord
lateinit var database: Database

fun printFancy(input: String) {
    val edges = "*".repeat(input.length + 4)
    logger.info(edges)
    logger.info("* $input *")
    logger.info(edges)
}

fun printStep(input: String, step: Int) {
    val add = "  ".repeat(step)
    logger.info(step.toString() + add + input)
}

suspend fun setupDatabase() {
    printStep("Setup database", 1)
    val file = File("proxyfox.db.properties")
    database = if (file.exists()) {
        val properties = Properties()
        file.inputStream().use(properties::load)
        val psql = PostgresDatabase(Driver())
        psql.startConnection(properties.getProperty("url"), properties)
        psql
    } else {
        NopDatabase()
    }
}

@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Logging in", 1)
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
            // Run the command
            val output = parseString(contentWithoutRegex, message)
            // Send output message if exists
            if (output!!.isNotBlank())
                message.channel.createMessage(output)
        } else {
            // Proxy the message
            val proxy = database.getProxyTagFromMessage(message.author!!.id, content)
            if (proxy != null) {
                val member = database.getMemberById(proxy.systemId, proxy.memberId)!!
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

@OptIn(DelicateCoroutinesApi::class)
fun runAsync(action: suspend () -> Unit): Int {
    GlobalScope.launch {
        action()
    }
    return 0
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <T> T.applyAsync(block: suspend T.() -> Unit): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return this
}