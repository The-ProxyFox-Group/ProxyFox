package dev.proxyfox.bot.command.menu

import dev.kord.core.entity.Message
import dev.proxyfox.command.menu.CommandMenu
import dev.proxyfox.command.menu.CommandScreen
import kotlinx.coroutines.Job

class DiscordMenu(val message: Message) : CommandMenu() {
    private val jobs = arrayListOf<Job>()

    override suspend fun close() {
        jobs.forEach {
            it.cancel()
        }
    }

    override suspend fun createScreen(name: String): CommandScreen {
        return DiscordScreen(name, this)
    }
}