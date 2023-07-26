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
import dev.proxyfox.command.Command
import dev.proxyfox.command.Context
import dev.proxyfox.command.LiteralArgument
import dev.proxyfox.command.types.GreedyString
import dev.proxyfox.command.types.UnixList
import dev.proxyfox.common.find

@Suppress("UNUSED")
@LiteralArgument("pluralkit", "pk")
object PkCommands {
    @Command
    suspend fun pull(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("pull", "get", "download", "import") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.syncPk(ctx, system, false)
    }
    @Command
    suspend fun push(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("push", "set", "upload", "export") literal: Unit
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.syncPk(ctx, system, true)
    }

    @Command
    suspend fun token(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("token") literal: Unit,
        unixValues: UnixList?,
        token: GreedyString?
    ) {
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MiscCommands.pkToken(ctx, system, token?.value, clear)
    }
}