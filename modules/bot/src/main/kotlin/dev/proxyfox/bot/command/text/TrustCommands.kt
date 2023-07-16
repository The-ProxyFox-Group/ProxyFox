/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.text

import dev.proxyfox.bot.command.MiscCommands
import dev.proxyfox.bot.command.checkSystem
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.types.CommandSnowflake
import dev.proxyfox.command.Command
import dev.proxyfox.command.Context
import dev.proxyfox.command.LiteralArgument
import dev.proxyfox.database.records.misc.TrustLevel

@Suppress("UNUSED")
@LiteralArgument("trust")
object TrustCommands {
    @Command
    suspend fun none(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake,
        @LiteralArgument("none", "remove", "clear") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, TrustLevel.NONE)
    }

    @Command
    suspend fun access(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake,
        @LiteralArgument("access", "see", "view") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, TrustLevel.ACCESS)
    }

    @Command
    suspend fun switch(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake,
        @LiteralArgument("switch", "sw") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, TrustLevel.SWITCH)
    }

    @Command
    suspend fun member(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake,
        @LiteralArgument("member", "mem", "m") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, TrustLevel.MEMBER)
    }

    @Command
    suspend fun full(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake,
        @LiteralArgument("full", "all", "everything") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, TrustLevel.FULL)
    }

    @Command
    suspend fun noLiteral(
        @Context ctx: DiscordContext<Any>,
        userId: CommandSnowflake
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.trust(ctx, system, userId.snowflake.value, null)
    }

    @Command
    suspend fun noId(
        @Context ctx: DiscordContext<Any>
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        ctx.respondFailure("Please provide a user to perform this action on")
    }
}