/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.text

import dev.proxyfox.bot.command.MiscCommands
import dev.proxyfox.bot.command.checkMember
import dev.proxyfox.bot.command.checkSystem
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.types.CommandAttachment
import dev.proxyfox.bot.command.types.CommandBoolean
import dev.proxyfox.bot.command.types.CommandProxyMode
import dev.proxyfox.bot.command.types.CommandSnowflake
import dev.proxyfox.bot.kord
import dev.proxyfox.command.Command
import dev.proxyfox.command.Context
import dev.proxyfox.command.LiteralArgument
import dev.proxyfox.command.types.GreedyString
import dev.proxyfox.command.types.UnixList
import dev.proxyfox.common.find
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.AutoProxyMode
import dev.proxyfox.database.records.misc.TokenType
import dev.proxyfox.database.records.misc.TrustLevel

@Suppress("UNUSED")
object MiscTextCommands {
    @Command
    suspend fun import(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("import") literal: Unit,
        attachment: CommandAttachment?,
        url: GreedyString?
    ) {
        val url = url?.value?.ifEmpty { null } ?: attachment?.attachment?.url

        MiscCommands.import(ctx, url)
    }

    @Command
    suspend fun export(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("export") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        MiscCommands.export(ctx)
    }

    @Command
    suspend fun time(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("time") literal: Unit
    ) = MiscCommands.time(ctx)

    @Command
    suspend fun help(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("help") literal: Unit
    ) = ctx.respondPlain(MiscCommands.help)

    @Command
    suspend fun explain(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("help") literal: Unit
    ) = ctx.respondPlain(MiscCommands.explain)

    @Command
    suspend fun invite(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("help") literal: Unit
    ) = ctx.respondPlain(MiscCommands.invite)

    @Command
    suspend fun source(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("help") literal: Unit
    ) = ctx.respondPlain(MiscCommands.source)

    @Command
    suspend fun debug(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("debug") literal: Unit
    ) = MiscCommands.debug(ctx)

    @Command
    suspend fun fox(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("fox") literal: Unit
    ) = MiscCommands.getFox(ctx)

    @Command
    suspend fun proxy(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("proxy", "p") literal: Unit,
        guildId: CommandSnowflake?,
        boolean: CommandBoolean?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        val guild = guildId?.let { kord.getGuild(guildId.snowflake) } ?: ctx.getGuild() ?: run {
            ctx.respondFailure("Cannot find guild.")
            return@proxy
        }

        val systemServer = database.getOrCreateServerSettingsFromSystem(guild, system.id)

        MiscCommands.serverProxy(ctx, systemServer, boolean?.value)
    }

    @Command
    suspend fun autoproxy(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("autoproxy", "ap") literal: Unit,
        mode: CommandProxyMode?,
        member: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        val member = member?.value?.let {
            val member = database.fetchMemberFromSystem(system.id, it)
            if (!checkMember(ctx, member)) return@autoproxy
            member
        }

        MiscCommands.proxy(ctx, system, mode?.value, member)
    }

    @Command
    suspend fun serverAutoproxy(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("serverautoproxy", "sap") literal: Unit,
        guildId: CommandSnowflake?,
        mode: CommandProxyMode?,
        member: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        val guild = guildId?.let { kord.getGuild(guildId.snowflake) } ?: ctx.getGuild() ?: run {
            ctx.respondFailure("Cannot find guild.")
            return@serverAutoproxy
        }

        val systemServer = database.getOrCreateServerSettingsFromSystem(guild, system.id)

        val member = member?.value?.let {
            val member = database.fetchMemberFromSystem(system.id, it)
            if (!checkMember(ctx, member)) return@serverAutoproxy
            member
        }

        val mode = mode?.value?.let { if (it == AutoProxyMode.OFF) AutoProxyMode.FALLBACK else it }

        MiscCommands.serverAutoProxy(ctx, systemServer, mode, member)
    }

    @Command
    suspend fun role(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("role") literal: Unit,
        unixValues: UnixList?,
        role: GreedyString?
    ) {
        val clear = unixValues.find("clear")

        MiscCommands.role(ctx, role?.value, clear)
    }

    @Command
    suspend fun modDelay(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("role") literal: Unit,
        guildId: CommandSnowflake?,
        delay: GreedyString?
    ) {
        val guild = guildId?.let { kord.getGuild(guildId.snowflake) } ?: ctx.getGuild() ?: run {
            ctx.respondFailure("Cannot find guild.")
            return@modDelay
        }

        val settings = database.getOrCreateServerSettings(guild)

        MiscCommands.delay(ctx, settings, delay?.value)
    }

    @Command
    suspend fun forceTag(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("forcetag", "requiretag") literal: Unit,
        boolean: CommandBoolean?
    ) = MiscCommands.forceTag(ctx, boolean?.value)

    @Command
    suspend fun deleteMessage(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("delete", "del") literal: Unit,
        messageId: CommandSnowflake?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.deleteMessage(ctx, system, messageId?.snowflake)
    }

    @Command
    suspend fun reproxyMessage(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("reproxy", "rp") literal: Unit,
        messageId: CommandSnowflake?,
        member: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        val member = member?.value?.let {
            val member = database.fetchMemberFromSystem(system.id, it)
            if (!checkMember(ctx, member)) return@reproxyMessage
            member
        }

        MiscCommands.reproxyMessage(ctx, system, messageId?.snowflake, member)
    }

    @Command
    suspend fun fetchMessage(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("info", "i") literal: Unit,
        messageId: CommandSnowflake?
    ) = MiscCommands.fetchMessageInfo(ctx, messageId?.snowflake)

    @Command
    suspend fun pingMessage(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("ping") literal: Unit,
        messageId: CommandSnowflake?
    ) = MiscCommands.pingMessageAuthor(ctx, messageId?.snowflake)

    @Command
    suspend fun editMessage(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("edit", "e") literal: Unit,
        messageId: CommandSnowflake?,
        content: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.editMessage(ctx, system, messageId?.snowflake, content?.value)
    }

    @Command
    suspend fun channelProxy(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("channelproxy", "cp") literal1: Unit,
        channelId: CommandSnowflake?,
        boolean: CommandBoolean?
    ) = MiscCommands.channelProxy(ctx,(channelId?.snowflake ?: ctx.getChannel(false).id).toString() , boolean?.value)

    @Command
    suspend fun token(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("token", "t") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.token(ctx, system)
    }

    suspend fun transfer(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("transfer") literal: Unit,
        token: GreedyString?
    ) {
        if (token == null) {
            ctx.respondFailure("Please provide a token to transfer from")
            return
        }

        if (ctx.getSys() != null) {
            ctx.respondFailure("You can only run this command when you have no system registered.")
            return
        }

        val token = database.fetchToken(token.value)
        if (token == null) {
            ctx.respondFailure("Token not found.")
            return
        }
        if (token.type != TokenType.SYSTEM_TRANSFER) {
            ctx.respondFailure("Token isn't a transfer token.")
            return
        }

        MiscCommands.transfer(ctx, token)
    }
}
