/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.common.printFancy
import dev.proxyfox.common.printStep
import dev.proxyfox.common.spacedDot
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.datetime.Instant
import java.time.OffsetDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

const val UPLOAD_LIMIT = 1024 * 1024 * 8

lateinit var scope: CoroutineScope
lateinit var kord: Kord
lateinit var http: HttpClient

@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Setting up HTTP client", 1)
    http = HttpClient(CIO)

    printStep("Setting up coroutine scope", 1)
    scope = CoroutineScope(Dispatchers.Default)

    printStep("Logging in", 1)
    kord = Kord(System.getenv("PROXYFOX_KEY"))

    // Register events
    printStep("Registering events", 2)

    kord.on<MessageCreateEvent> {
        onMessageCreate()
    }

    kord.on<ReactionAddEvent> {
        onReactionAdd()
    }

    kord.on<ReadyEvent> {
        printFancy("ProxyFox initialized")
        scope.launch {
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
        // TODO: Cycle between "in x servers", "x systems registered", and "Uptime: DD:HH:MM
        kord.editPresence {
            val servers = kord.guilds.count()
            playing("Run pf>help for help! in $servers servers!")
        }
        delay(1800000)
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

fun OffsetDateTime.toKtInstant() = Instant.fromEpochSeconds(epochSeconds = toEpochSecond(), nanosecondAdjustment = nano)

fun Int.kordColor() = if (this < 0) null else Color(this)

suspend fun EmbedBuilder.member(record: MemberRecord, serverId: ULong) {
    color = record.color.kordColor()
    author {
        name = record.serverName(serverId) ?: record.displayName ?: record.name
        icon = record.avatarUrl
    }
}

suspend fun EmbedBuilder.system(
    record: SystemRecord,
    nameTransformer: (String) -> String = { it },
    footerTransformer: (String) -> String = { "System ID$spacedDot$it" }
) {
    color = record.color.kordColor()
    author {
        name = nameTransformer(record.name ?: record.id)
        icon = record.avatarUrl
    }
    footer {
        text = footerTransformer(record.id)
    }
}

/**
 * Reaction-based yes/no prompt helper method for message commands.
 *
 * @receiver The message for the reaction prompt.
 * @param timeout The duration until the prompt is no longer valid.
 * @param runner The runner of the command. May also be anyone else depending on the context.
 * @param yes The action to run on a check reaction.
 * @param no The action to run on an X reaction.
 * */
suspend fun Message.timedYesNoPrompt(
    timeout: Duration = 1.minutes,
    runner: Snowflake,
    yes: suspend Message.() -> Unit,
    no: suspend Message.() -> Unit = { channel.createMessage("Action cancelled.") }
) {
    addReaction(ReactionEmoji.Unicode("❌"))
    addReaction(ReactionEmoji.Unicode("✅"))
    var job: Job? = null
    val micro = scope.launch {
        delay(timeout)
        channel.createMessage("Timed out.")
        job?.cancel()
    }
    job = kord.on<ReactionAddEvent> {
        if (messageId == id && userId == runner) {
            when (emoji.name) {
                "✅" -> {
                    yes()
                    job!!.cancel()
                }

                "❌" -> {
                    no()
                    job!!.cancel()
                }
            }
        }
    }
    job.invokeOnCompletion { micro.cancel() }
}