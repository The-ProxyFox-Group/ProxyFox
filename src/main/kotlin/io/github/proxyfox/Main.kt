@file:JvmName("Main")

package io.github.proxyfox

import io.github.proxyfox.command.CommandSource
import io.github.proxyfox.command.Commands
import io.github.proxyfox.command.dispatcher
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import io.github.proxyfox.database.Database
import io.github.proxyfox.database.NopDatabase
import io.github.proxyfox.importer.gson
import io.github.proxyfox.webhook.WebhookCache
import io.github.proxyfox.webhook.WebhookUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun printFancy(input: String) {
    val edges = "*".repeat(input.length+4)
    println(edges)
    println("* $input *")
    println(edges)
}
fun printStep(input: String, step: Int) {
    val add = "-".repeat(step)
    println(step.toString()+add+input)
}

val prefixRegex = Regex("^pf[>;!].*",RegexOption.IGNORE_CASE)

lateinit var kord: Kord
val database: Database = NopDatabase()

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    printFancy("Initializing ProxyFox")
    // Register commands in brigadier
    Commands.register()

    // Login to Kord and set up events
    kord = Kord(System.getenv("PROXYFOX_KEY"))
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