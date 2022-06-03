package dev.proxyfox

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.proxyfox.database.Database
import dev.proxyfox.database.JsonDatabase
import dev.proxyfox.database.MongoDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
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

fun String.toColor(): Int {
    return if (startsWith("#"))
        Integer.valueOf(substring(1), 16)
    else if (startsWith("0x"))
        Integer.decode(this)
    else toInt(16)
}

fun Int.fromColor(): String {
    var string = toString(16)
    if (string == "-1") string = "0"
    return "#${string.padStart(7 - string.length, '0')}"
}

suspend fun setupDatabase() {
    printStep("Setup database", 1)
    database = try {
        val db = MongoDatabase()
        db.setup()
        db
    } catch (err: Throwable) {
        printStep("Database setup failed. Falling back to JSON", 2)
        val db = JsonDatabase()
        db.setup()
        db
    }
}

@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Logging in", 1)
    kord = Kord(System.getenv("PROXYFOX_KEY"))

    // Register events
    printStep("Registering events", 2)
    kord.on<MessageCreateEvent> {
        onMessageCreate()
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