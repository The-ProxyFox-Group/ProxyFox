import command.CommandSource
import command.Commands
import command.dispatcher
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch

val prefixRegex = Regex("^pf[>;!].*",RegexOption.IGNORE_CASE)

lateinit var kord: Kord

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    println("Initializing ProxyFox")
    Commands.register()
    kord = Kord(System.getenv("PROXYFOX_KEY"))
    kord.on<MessageCreateEvent> {
        val content = message.content
        if (prefixRegex.matches(content)) {
            val contentWithoutRegex = content.substring(3)
            dispatcher.execute(contentWithoutRegex,CommandSource(message))
        } else {
            // TODO: Send proxy
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