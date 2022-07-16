package dev.proxyfox.bot

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.proxyfox.common.printFancy
import dev.proxyfox.common.printStep
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch

lateinit var kord: Kord
lateinit var http: HttpClient

@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Setting up HTTP client", 1)
    http = HttpClient(CIO)

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

fun findUnixValue(args: Array<String>, key: String): String? {
    for (i in args.indices) {
        if (args[i].startsWith(key)) {
            return args[i].substring(key.length)
        }
    }
    return null
}