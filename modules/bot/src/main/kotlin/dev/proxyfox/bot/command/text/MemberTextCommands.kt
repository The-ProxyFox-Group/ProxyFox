/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.text

import dev.proxyfox.bot.command.MemberCommands
import dev.proxyfox.bot.command.checkMember
import dev.proxyfox.bot.command.checkSystem
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.types.CommandAttachment
import dev.proxyfox.bot.command.types.CommandBoolean
import dev.proxyfox.bot.command.types.CommandSnowflake
import dev.proxyfox.bot.kord
import dev.proxyfox.command.Command
import dev.proxyfox.command.Context
import dev.proxyfox.command.LiteralArgument
import dev.proxyfox.command.types.GreedyString
import dev.proxyfox.command.types.UnixList
import dev.proxyfox.common.find
import dev.proxyfox.common.toColor
import dev.proxyfox.database.database
import dev.proxyfox.database.tryParseLocalDate


@Suppress("UNUSED")
@LiteralArgument("member", "mem", "m")
object MemberTextCommands {
    @Command
    suspend fun delete(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("delete", "remove", "del") literal: Unit,
        memberId: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        if (memberId == null) {
            MemberCommands.delete(ctx, system, null)
            return
        }
        val member = database.fetchMemberFromSystem(system.id, memberId.value)
        if (!checkMember(ctx, member)) return
        MemberCommands.delete(ctx, system, member)
    }

    @Command
    suspend fun delete(
        @Context ctx: DiscordContext<Any>,
        memberId: GreedyString?,
        @LiteralArgument("delete", "remove", "del") literal: Unit
    ) = delete(ctx, Unit, memberId)

    @Command
    suspend fun create(
        @Context ctx: DiscordContext<Any>,
        @LiteralArgument("create", "c", "new", "add") literal: Unit,
        name: GreedyString?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        MemberCommands.create(ctx, system, name?.value)
    }

    @Command
    suspend fun name(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("name", "rename") literal: Unit,
        unixValues: UnixList?,
        name: GreedyString?
    ) {
        val raw = unixValues.find("raw")

        val name = name?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.rename(ctx, system, member, name, raw)
    }

    @Command
    suspend fun nickname(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("nickname", "nick", "displayname", "dn") literal: Unit,
        unixValues: UnixList?,
        name: GreedyString?
    ) {
        val raw = unixValues.find("raw")
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val name = name?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.nickname(ctx, system, member, name, raw, clear)
    }

    @Command
    suspend fun serverNick(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("servername", "servernick", "sn") literal: Unit,
        guildId: CommandSnowflake?,
        unixValues: UnixList?,
        name: GreedyString?
    ) {
        val raw = unixValues.find("raw")
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val name = name?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        val guild = guildId?.let { kord.getGuild(guildId.snowflake) } ?: ctx.getGuild() ?: run {
            ctx.respondFailure("Cannot find guild.")
            return@serverNick
        }

        val serverMember =
            database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)!!

        MemberCommands.servername(ctx, system, serverMember, name, raw, clear)
    }

    @Command
    suspend fun description(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("description", "desc") literal: Unit,
        unixValues: UnixList?,
        desc: GreedyString?
    ) {
        val raw = unixValues.find("raw")
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val desc = desc?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.description(ctx, system, member, desc, raw, clear)
    }

    @Command
    suspend fun avatar(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("avatar", "pfp") literal: Unit,
        unixValues: UnixList?,
        attachment: CommandAttachment?,
        url: GreedyString?
    ) {
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val url = url?.value?.ifEmpty { null } ?: attachment?.attachment?.url

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.avatar(ctx, system, member, url, clear)
    }

    @Command
    suspend fun serverAvatar(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("serveravatar", "sa", "serverpfp", "sp") literal: Unit,
        guildId: CommandSnowflake?,
        unixValues: UnixList?,
        attachment: CommandAttachment?,
        url: GreedyString?
    ) {
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val url = url?.value?.ifEmpty { null } ?: attachment?.attachment?.url

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        val guild = guildId?.let { kord.getGuild(guildId.snowflake) } ?: ctx.getGuild() ?: run {
            ctx.respondFailure("Cannot find guild.")
            return@serverAvatar
        }

        val serverMember =
            database.fetchMemberServerSettingsFromSystemAndMember(guild, system.id, member.id)!!

        MemberCommands.serverAvatar(ctx, system, serverMember, url, clear)
    }

    @Command
    suspend fun autoproxy(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("autoproxy", "ap") literal: Unit,
        boolean: CommandBoolean?
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.autoproxy(ctx, system, member, boolean?.value)
    }

    @Command
    suspend fun proxyAdd(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("autoproxy", "ap") literal1: Unit,
        @LiteralArgument("add", "create", "new") literal2: Unit,
        proxy: GreedyString?
    ) {
        val proxytag = proxy?.value

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        if (proxytag == null) {
            MemberCommands.proxy(ctx, system, member, null)
            return
        }

        val proxy = extractProxyFromTag(ctx, proxytag) ?: return

        MemberCommands.proxy(ctx, system, member, proxy)
    }

    @Command
    suspend fun proxyDelete(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("autoproxy", "ap") literal1: Unit,
        @LiteralArgument("remove", "rem", "delete", "del") literal2: Unit,
        proxy: GreedyString?
    ) {
        val proxytag = proxy?.value

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        if (proxytag == null || !proxytag.contains("text") ) {
            MemberCommands.removeProxy(ctx, system, member, false, null)
            return
        }

        val proxyDb = database.fetchProxyTagFromMessage(ctx.getUser(), proxytag)
        proxyDb ?: run {
            ctx.respondFailure("Proxy tag doesn't exist in this member.")
            return@proxyDelete
        }
        if (proxyDb.memberId != member.id) {
            ctx.respondFailure("Proxy tag doesn't exist in this member.")
            return
        }

        MemberCommands.removeProxy(ctx, system, member, true, proxyDb)
    }

    @Command
    suspend fun proxy(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("autoproxy", "ap") literal: Unit,
        proxy: GreedyString?
    ) = proxyAdd(ctx, memberId, Unit, Unit, proxy)

    @Command
    suspend fun pronouns(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("pronouns", "bluehair") pronounsliteral: Unit,
        unixValues: UnixList?,
        pronouns: GreedyString?
    ) {
        val raw = unixValues.find("raw")
        val clear = unixValues.find("clear") || unixValues.find("remove")

        val pronouns = pronouns?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.pronouns(ctx, system, member, pronouns, raw, clear)
    }

    @Command
    suspend fun color(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("color", "colour") literal: Unit,
        color: GreedyString?
    ) {
        val color = color?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.color(ctx, system, member, color?.toColor())
    }

    @Command
    suspend fun birthday(
        @Context ctx: DiscordContext<Any>,
        memberId: String,
        @LiteralArgument("birthday", "bday", "birth", "bd") literal: Unit,
        unixValues: UnixList?,
        birthday: GreedyString?
    ) {
        val clear = unixValues.find("clear")

        val birthday = birthday?.value?.ifEmpty { null }

        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.birthday(ctx, system, member, tryParseLocalDate(birthday)?.first, clear)
    }

    @Command
    suspend fun access(
        @Context ctx: DiscordContext<Any>,
        memberId: String
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return
        val member = database.fetchMemberFromSystem(system.id, memberId)
        if (!checkMember(ctx, member)) return

        MemberCommands.access(ctx, system, member)
    }

    @Command
    suspend fun empty(
        @Context ctx: DiscordContext<Any>
    ) {
        val system = ctx.getSys()
        if (!checkSystem(ctx, system)) return

        MemberCommands.empty(ctx)
    }

    suspend fun <T> extractProxyFromTag(ctx: DiscordContext<T>, proxy: String): Pair<String?, String?>? {
        if (!proxy.contains("text")) {
            ctx.respondFailure("Given proxy tag does not contain `text`.")
            return null
        }
        val prefix = proxy.substring(0, proxy.indexOf("text"))
        val suffix = proxy.substring(4 + prefix.length, proxy.length)
        if (prefix.isEmpty() && suffix.isEmpty()) {
            ctx.respondFailure("Proxy tag must contain either a prefix or a suffix.")
            return null
        }
        return Pair(prefix, suffix)
    }
}
