/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import dev.kord.common.Color
import dev.kord.common.EmptyBitSet
import dev.kord.common.entity.*
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.builder.kord.KordBuilder
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.*
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.builder.Shards
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.json.request.ApplicationCommandCreateRequest
import dev.kord.rest.request.KtorRequestException
import dev.proxyfox.bot.command.*
import dev.proxyfox.bot.command.interaction.ProxyFoxChatInputCreateBuilderImpl
import dev.proxyfox.common.*
import dev.proxyfox.database.database
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.markt.MarkdownParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.fold
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.lang.Integer.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

const val UPLOAD_LIMIT = 1024 * 1024 * 8

val scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())

suspend fun ScheduledExecutorService.schedule(duration: Duration, action: suspend () -> Unit) {
    schedule({
        runBlocking {
            action()
        }
    }, duration.toLong(DurationUnit.SECONDS), TimeUnit.SECONDS)
}

private val idUrl = System.getenv("PROXYFOX_KEY").let { it.substring(0, it.indexOf('.')) }

private val webhook = Regex("https?://(?:[^./]\\.)?discord(?:app)?\\.com/api/(v\\d+/)?webhooks/\\d+/\\S+")
private val token = Regex("$idUrl[a-z0-9=/+_-]*\\.[a-z0-9=/+_-]+\\.[a-z0-9=/+_-]", RegexOption.IGNORE_CASE)

lateinit var scope: CoroutineScope
lateinit var kord: Kord
lateinit var http: HttpClient
lateinit var startTime: Instant
private var count: Int = 0
var shardCount: Int = 0
val errorChannelId = try {
    Snowflake(System.getenv("PROXYFOX_LOG"))
} catch (_: Throwable) {
    null
}
var errorChannel: TextChannel? = null

val markdownParser = MarkdownParser()

@OptIn(PrivilegedIntent::class)
suspend fun login() {
    printStep("Setting up HTTP client", 1)
    http = HttpClient(CIO)

    printStep("Setting up coroutine scope", 1)
    scope = CoroutineScope(Dispatchers.Default)

    printStep("Logging in", 1)
    val builder = KordBuilder(System.getenv("PROXYFOX_KEY"))

    builder.sharding {
        val shards = System.getenv("PROXYFOX_MAX_SHARDS")?.toIntOrNull() ?: (it+2)
        shardCount = min(it+2, shards)

        printStep("Setting up sharding with $shardCount shards", 2)
        Shards(shardCount)
    }

    kord = builder.build()

    // Register events
    printStep("Registering events", 2)

    kord.on<MessageCreateEvent> {
        try {
            onMessageCreate()
        } catch (err: Throwable) {
            handleError(err, message)
        }
    }

    kord.on<MessageUpdateEvent> {
        try {
            onMessageUpdate()
        } catch (err: Throwable) {
            handleError(err, message)
        }
    }

    kord.on<ReactionAddEvent> {
        onReactionAdd()
    }

    kord.on<ButtonInteractionCreateEvent> {
        if (interaction.componentId == "free-delete") {
            interaction.message.delete("User requested deletion.")
        }
    }

    kord.on<ModalSubmitInteractionCreateEvent> {
        handleModal()
    }

    kord.registerCommands()

    kord.on<MessageCommandInteractionCreateEvent> {
        onInteract()
    }
    kord.on<ChatInputCommandInteractionCreateEvent> {
        onInteract()
    }

    var initialized = false
    kord.on<ReadyEvent> {
        if (!initialized) {
            printFancy("ProxyFox initialized")
            scope.launch {
                updatePresence()
            }
            initialized = true
        }

        logger.info("Shard online: $shard")
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

        presence {
            watching("for pf>help!")
        }
    }
}

suspend fun Kord.registerCommands() {
    printStep("Registering commands", 2)
    createGlobalMessageCommand("Delete Message")
    createGlobalMessageCommand("Fetch Message Info")
    createGlobalMessageCommand("Ping Message Author")
    createGlobalMessageCommand("Edit Message")
    Commands {
        +SystemCommands
        +MemberCommands
        +GroupCommands
        +SwitchCommands
        +MiscCommands
    }

    scope.launch {
        rest.interaction.createGlobalApplicationCommands(
            resources.applicationId,
            deferredCommands
        )
    }
}

val deferredCommands = arrayListOf<ApplicationCommandCreateRequest>()

fun deferChatInputCommand(
    name: String,
    description: String,
    builder: GlobalChatInputCreateBuilder.() -> Unit = {}
) {
    deferredCommands.add(ProxyFoxChatInputCreateBuilderImpl(name, description).apply(builder).toRequest())
}

suspend fun updatePresence() {
    startTime = Clock.System.now()
    scheduler.fixedRateAction(Duration.ZERO, 2.minutes) {
        count = (count + 1) % 3
        val append = when (count) {
            0 -> {
                val servers = kord.guilds.count()
                "in $servers servers!"
            }

            1 -> {
                val systemCount = database.fetchTotalSystems()
                "$systemCount systems registered!"
            }

            2 -> {
                "uptime: ${(Clock.System.now() - startTime).inWholeHours} hours!"
            }

            else -> throw AssertionError("Count ($count) not in 0..2")
        }
        kord.editPresence {
            watching("for /info help! $append")
        }
    }
}

suspend fun handleError(err: Throwable, interaction: ChatInputCommandInteractionCreateEvent) =
    handleError(err, interaction.interaction.channel)

suspend fun handleError(err: Throwable, message: MessageBehavior) = handleError(err, message.channel)

suspend fun handleError(err: Throwable, channel: MessageChannelBehavior) {
    // Catch any errors and log them
    val timestamp = System.currentTimeMillis()
    // Let the logger unwind the stacktrace.
    logger.warn(timestamp.toString(), err)
    // Do not leak webhook URL nor token in output.
    // Note: The token here is a generic regex that only matches with the bot's
    // ID and will make no attempt to verify it's the real one, purely for guarding
    // the token from brute forcing of the replace method.
    val reason = err.message?.replace(webhook, "[WEBHOOK]")?.replace(token, "[TOKEN]")
    var cause = ""
    err.stackTrace.forEach {
        if (it.className.startsWith("dev.proxyfox"))
            cause += "  at $it\n"
    }
    channel.createMessage(
        "An unexpected error occurred.\nTimestamp: `$timestamp`\n```\n${err.javaClass.name}: $reason\n$cause```"
    )
    if (err is DebugException) return
    if (errorChannel == null && errorChannelId != null)
        errorChannel = kord.getChannel(errorChannelId) as TextChannel
    if (errorChannel != null) {
        // Prevent the log channel from also showing tokens, should it be public in any manner.
        cause = err.stackTraceToString().replace(webhook, "[WEBHOOK]").replace(token, "[TOKEN]")

        errorChannel!!.createMessage {
            content = "`$timestamp`"
            addFile("exception.log", ChannelProvider { cause.byteInputStream().toByteReadChannel() })
        }
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
fun hasUnixValue(args: Array<String>, key: String): Boolean {
    for (i in args.indices) {
        if (args[i].startsWith(key)) {
            return true
        }
    }
    return true
}

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

fun ULong.toShard() = if (shardCount == 0) 0 else ((this shr 22) % shardCount.toULong()).toInt()

fun String.parseDuration(): Either<Duration, String> {
    var years = 0L
    var weeks = 0L
    var days = 0L
    var hours = 0L
    var minutes = 0L
    var seconds = 0L
    var current = 0L

    for (i in this) {
        if (i.isDigit()) current = current * 10 + (i - '0')
        else when (i) {
            '-' -> {
                current = -current
            }

            'y' -> {
                if (years != 0L) return Either.ofB("Provided time string contains multiple year declarations.")
                years = current
                current = 0
            }

            'w' -> {
                if (weeks != 0L) return Either.ofB("Provided time string contains multiple week declarations.")
                weeks = current
                current = 0
            }

            'd' -> {
                if (days != 0L) return Either.ofB("Provided time string contains multiple day declarations.")
                days = current
                current = 0
            }

            'h' -> {
                if (hours != 0L) return Either.ofB("Provided time string contains multiple hour declarations.")
                hours = current
                current = 0
            }

            'm' -> {
                if (minutes != 0L) return Either.ofB("Provided time string contains multiple minute declarations.")
                minutes = current
                current = 0
            }

            's' -> {
                if (seconds != 0L) return Either.ofB("Provided time string contains multiple second declarations.")
                seconds = current
                current = 0
            }
        }
    }

    if (weeks != 0L) days += weeks * 7

    val totalSeconds = (((((years * 365 + days) * 24 + hours) * 60 + minutes) * 60) + seconds) * 1000 + current

    return Either.ofA(totalSeconds.milliseconds)
}

@JvmRecord
data class Either<A, B>(
    val left: A?,
    val right: B?,
) {
    companion object {
        fun <A, B> ofA(a: A) = Either<A, B>(a, null)

        fun <A, B> ofB(b: B) = Either<A, B>(null, b)
    }
}

val GuildMessageChannel.sendPermission
    get() = if (this is ThreadChannel) Permission.SendMessagesInThreads else Permission.SendMessages

suspend fun GuildMessageChannel.permissionHolder() = if (this is ThreadChannel) kord.getChannel(parentId)!! else this

suspend fun MessageChannelBehavior.selfCanSend(): Boolean {
    return if (this is GuildMessageChannel) selfHasPermissions(Permissions(sendPermission, Permission.ViewChannel)) else true
}

suspend fun GuildMessageChannel.selfHasPermissions(permissions: Permissions): Boolean {
    return permissionHolder().getEffectivePermissions(guild.getMember(kord.selfId)).contains(permissions)
}

suspend fun GuildMessageChannel.selfHasPermissions(permission: Permission): Boolean {
    return permissionHolder().getEffectivePermissions(guild.getMember(kord.selfId)).contains(permission)
}

suspend fun Channel.getEffectivePermissions(member: Member): Permissions {
    val map = data.permissionOverwrites.value.orEmpty().associateBy { it.id }

    val effective = EmptyBitSet()
    effective.add(member.getPermissions().code)

    val deny = EmptyBitSet()
    val allow = EmptyBitSet()

    val out = member.roles.fold(deny to allow) { acc, value ->
        map[value.id]?.let {
            acc.first.add(it.deny.code)
            acc.second.add(it.allow.code)
        }
        // TODO: Verify this is correct
        acc
    }

    assert(out.first === deny)
    assert(out.second === allow)

    effective.remove(deny)
    effective.add(allow)

    map[member.id]?.let {
        effective.remove(it.deny.code)
        effective.add(it.allow.code)
    }

    return Permissions(effective)
}

@OptIn(ExperimentalContracts::class)
inline fun <T> nullOn404(action: () -> T): T? {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    try {
        return action()
    } catch (e: KtorRequestException) {
        if (e.httpResponse.status == HttpStatusCode.NotFound) {
            return null
        }
        throw e
    }
}