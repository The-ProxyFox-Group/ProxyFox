package dev.proxyfox.bot.command.menu

import dev.kord.core.entity.Message
import dev.proxyfox.command.menu.CommandScreen

class DiscordScreen(name: String, private val menu: DiscordMenu) : CommandScreen(name) {
    @Suppress("UNUSED")
    private val message: Message get() = menu.message

    override suspend fun init() {

    }
}