/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.context

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.bot.kord
import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.NodeActionParam
import dev.proxyfox.command.NodeHolder
import dev.proxyfox.command.node.CommandNode
import dev.proxyfox.command.node.builtin.int

abstract class DiscordContext<T>(override val value: T) : CommandContext<T>() {
    abstract fun getAttachment(): Attachment?
    abstract suspend fun getChannel(private: Boolean = false): MessageChannelBehavior
    abstract suspend fun getGuild(): Guild?
    abstract suspend fun getUser(): User?
    abstract suspend fun getMember(): Member?
    abstract suspend fun respondFiles(text: String? = null, vararg files: NamedFile): T
    abstract suspend fun respondEmbed(private: Boolean = false, text: String? = null, embed: suspend EmbedBuilder.() -> Unit): T
    abstract suspend fun tryDeleteTrigger(reason: String? = null)

    suspend fun hasRequired(permission: Permission): Boolean {
        val author = getMember() ?: return false
        return author.getPermissions().contains(permission)
    }
}

// Get a DiscordContext.
fun <T, C: DiscordContext<T>> CommandNode<T, C>.runs(action: suspend DiscordContext<T>.() -> Boolean) {
    executes(action as suspend CommandContext<T>.() -> Boolean)
}

suspend fun<T, C: DiscordContext<T>> CommandNode<T, C>.guild(action: NodeActionParam<T, C, Snowflake?>) {
    action {
        val ctx = this as? DiscordContext<T> ?: return@action null
        ctx.getGuild()?.id
    }
    int("server") {
        action {
            Snowflake(it())
        }
    }
}