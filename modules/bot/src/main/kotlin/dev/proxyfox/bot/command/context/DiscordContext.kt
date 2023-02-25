/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.context

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.*
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.proxyfox.bot.Emojis
import dev.proxyfox.bot.command.menu.DiscordMenu
import dev.proxyfox.bot.command.menu.DiscordScreen
import dev.proxyfox.bot.schedule
import dev.proxyfox.bot.scheduler
import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.NodeActionParam
import dev.proxyfox.command.menu.CommandMenu
import dev.proxyfox.command.node.CommandNode
import dev.proxyfox.command.node.builtin.int
import dev.proxyfox.command.node.builtin.string
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

abstract class DiscordContext<T>(override val value: T) : CommandContext<T>() {
    abstract fun getAttachment(): Attachment?
    abstract suspend fun getChannel(private: Boolean = false): MessageChannelBehavior
    abstract suspend fun getGuild(): Guild?
    abstract suspend fun getUser(): User?
    abstract suspend fun getMember(): Member?
    abstract suspend fun respondEmbed(
        private: Boolean = false,
        text: String? = null,
        embed: suspend EmbedBuilder.() -> Unit
    ): T

    abstract suspend fun tryDeleteTrigger(reason: String? = null)

    abstract suspend fun optionalSuccess(text: String): T

    suspend fun respondFiles(text: String? = null, vararg files: NamedFile): Message = getChannel(true).createMessage {
        content = text
        this.files.addAll(files)
    }

    suspend fun hasRequired(permission: Permission): Boolean {
        val author = getMember() ?: return false
        return author.getPermissions().contains(permission)
    }

    abstract suspend fun getDatabaseMessage(
        system: SystemRecord?,
        messageId: Snowflake?
    ): Pair<Message?, ProxiedMessageRecord?>

    override suspend fun menu(action: suspend CommandMenu.() -> Unit) {
        interactionMenu {
            action()
        }
    }

    abstract suspend fun interactionMenu(private: Boolean = false, action: suspend DiscordMenu.() -> Unit)

    suspend fun timedYesNoPrompt(
        message: String,
        yes: Pair<String, suspend MessageModifyBuilder.() -> Unit>,
        no: Pair<String, suspend MessageModifyBuilder.() -> Unit> = "Cancel" to {
            content = "Action cancelled."
            components = null
        },
        timeout: Duration = 1.minutes,
        yesEmoji: DiscordPartialEmoji = Emojis.check,
        noEmoji: DiscordPartialEmoji = Emojis.multiply,
        timeoutAction: suspend MessageModifyBuilder.() -> Unit = no.second,
        danger: Boolean = false,
        public: Boolean = true
    ) {
        interactionMenu {
            default {
                this as DiscordScreen
                onInit {
                    edit {
                        content = message
                        actionRow {
                            interactionButton(if (danger) ButtonStyle.Danger else ButtonStyle.Primary, "yes") {
                                emoji = yesEmoji
                                label = yes.first
                            }
                            interactionButton(ButtonStyle.Secondary, "no") {
                                emoji = noEmoji
                                label = no.first
                            }
                        }
                    }
                    scheduler.schedule(timeout) {
                        if (!closed) {
                            edit {
                                timeoutAction()
                            }
                            close()
                        }
                    }
                }
                button("yes") {
                    edit(yes.second)
                }
                button("no") {
                    edit(no.second)
                }
            }
        }
    }
}

// Get a DiscordContext.
fun <T, C: DiscordContext<T>> CommandNode<T, C>.runs(action: suspend DiscordContext<T>.() -> Boolean) {
    @Suppress("UNCHECKED_CAST")
    executes(action as suspend CommandContext<T>.() -> Boolean)
}

suspend fun <T, C : DiscordContext<T>> CommandNode<T, C>.guild(action: NodeActionParam<T, C, Snowflake?>) {
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

//suspend fun <T, C: DiscordContext<T>> CommandNode<T, C>.id(name: String, action: NodeActionParam<T, C, Snowflake?>) {
//    string(name) {
//
//    }
//}

suspend fun <T, C : DiscordContext<T>> CommandNode<T, C>.system(action: NodeActionParam<T, C, SystemRecord?>) {
    action {
        val ctx = this as? DiscordContext<T> ?: return@action null
        database.fetchSystemFromUser(ctx.getUser())
    }
    string("sysid") {
        action {
            val id = it()
            (database.fetchSystemFromId(id) ?: database.fetchSystemFromUser(
                id.toULongOrNull() ?: return@action null
            ))?.let {
                if (!it.canAccess((this@action as DiscordContext<T>).getUser()!!.id.value))
                    return@action null
                return@action it
            }
        }
    }
}

suspend fun <T, C : DiscordContext<T>> CommandNode<T, C>.responds(content: String) {
    runs {
        respondPlain(content)
        true
    }
}
