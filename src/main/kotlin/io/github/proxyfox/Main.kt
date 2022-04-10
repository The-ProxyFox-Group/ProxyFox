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
import io.github.proxyfox.importer.gson
import io.github.proxyfox.webhook.WebhookCache
import io.github.proxyfox.webhook.WebhookUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun numberToNewId(int: Int): String {
    var out = ""
    var i1 = int
    for (ignore in arrayOf(1,2,3,4,5)) {
        val i2 = int % 26
        i1 -= i2
        i1 /= 26
        out = (i2.toChar()) + out
    }
    return out
}

val prefixRegex = Regex("^pf[>;!].*",RegexOption.IGNORE_CASE)

lateinit var kord: Kord

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    println("Initializing ProxyFox")
    Commands.register()
    kord = Kord(System.getenv("PROXYFOX_KEY"))
    kord.on<MessageCreateEvent> {
        if (message.webhookId != null || message.author!!.isBot) return@on
        val content = message.content
        if (prefixRegex.matches(content)) {
            val contentWithoutRegex = content.substring(3)
            dispatcher.execute(contentWithoutRegex,CommandSource(message))
        } else {
            WebhookUtil.prepareMessage(message)
        }
    }
    kord.on<ReadyEvent> {
        println("ProxyFox initialized")
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
    kord.editPresence {
        val servers = kord.guilds.count()
        playing("Run pf>help for help! in $servers servers!")
    }
    delay(30000)
    updatePresence()
}