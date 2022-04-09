import command.CommandSource
import command.Commands
import command.dispatcher
import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

val prefixRegex = Regex("^pf[>;!].*",RegexOption.IGNORE_CASE)

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    Commands.register()
    val kord = Kord(System.getenv("PROXYFOX_KEY"))
    kord.on<MessageCreateEvent> {
        val source = CommandSource(message)
        val content = message.content
        if (prefixRegex.matches(content)) {
            val contentWithoutRegex = content.substring(3)
            dispatcher.execute(contentWithoutRegex,source)
        } else {
            // TODO: Send proxy
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