package dev.proxyfox.bot.command

interface CommandRegistrar {
    val displayName: String

    suspend fun registerTextCommands()
    suspend fun registerSlashCommands()
}
