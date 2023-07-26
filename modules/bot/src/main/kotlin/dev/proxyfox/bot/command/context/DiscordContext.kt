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
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.proxyfox.bot.Emojis
import dev.proxyfox.bot.command.menu.DiscordMenu
import dev.proxyfox.bot.command.menu.DiscordScreen
import dev.proxyfox.bot.schedule
import dev.proxyfox.bot.scheduler
import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.NodeActionParam
import dev.proxyfox.command.menu.CommandMenu
import dev.proxyfox.command.menu.CommandScreen
import dev.proxyfox.command.node.CommandNode
import dev.proxyfox.command.node.builtin.int
import dev.proxyfox.command.node.builtin.string
import dev.proxyfox.common.ceilDiv
import dev.proxyfox.database.database
import dev.proxyfox.database.records.misc.ProxiedMessageRecord
import dev.proxyfox.database.records.system.SystemRecord
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

abstract class DiscordContext<T>(override val value: T) : CommandContext<T>() {
    override suspend fun respondFailure(text: String, private: Boolean): T = respondPlain("❌ $text", private)
    override suspend fun respondSuccess(text: String, private: Boolean): T = respondPlain("✅️ $text", private)
    override suspend fun respondWarning(text: String, private: Boolean): T = respondPlain("⚠️ $text", private)

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

    abstract suspend fun deferResponse(private: Boolean = false)

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

    suspend fun getSys(system: String? = null): SystemRecord? =
        if (system == null)
            database.fetchSystemFromUser(getUser()!!.id)
        else database.fetchSystemFromId(system)
    suspend fun timedYesNoPrompt(
        message: String,
        yes: Pair<String, suspend MessageModifyBuilder.() -> Unit>,
        no: Pair<String, suspend MessageModifyBuilder.() -> Unit> = "Cancel" to {
            content = "Action cancelled."
        },
        timeout: Duration = 1.minutes,
        yesEmoji: DiscordPartialEmoji = Emojis.check,
        noEmoji: DiscordPartialEmoji = Emojis.multiply,
        timeoutAction: suspend MessageModifyBuilder.() -> Unit = no.second,
        danger: Boolean = false,
        private: Boolean = false
    ) {
        interactionMenu(private) {
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
                                components = arrayListOf()
                                timeoutAction()
                            }
                            close()
                        }
                    }
                }
                button("yes") {
                    edit {
                        components = arrayListOf()
                        yes.second(this)
                    }
                    close()
                }
                button("no") {
                    edit {
                        components = arrayListOf()
                        no.second(this)
                    }
                    close()
                }
            }
        }
    }

    suspend fun <T> pager(
        values: List<T>,
        pageSize: Int,
        embedBuilder: suspend EmbedBuilder.(String) -> Unit,
        getString: suspend T.() -> String,
        private: Boolean
    ) {
        val pages = ceilDiv(values.size, pageSize)
        var page = 0

        interactionMenu(private) {
            lateinit var default: CommandScreen
            val select = "select" {
                this as DiscordScreen
                select("page") {
                    page = it[0].toInt()
                    setScreen(default)
                }

                onInit {
                    edit {
                        content = "Select a page"
                        embeds = mutableListOf()
                        actionRow {
                            stringSelect("page") {
                                for (i in 0 until pages) {
                                    option("${i+1}", "$i")
                                }
                            }
                        }
                    }
                }
            }
            default = default {
                this as DiscordScreen
                val update: suspend () -> Unit = {
                    edit {
                        content = null
                        embeds = mutableListOf()
                        embed {
                            embedBuilder("${page+1} / $pages")
                            var str = ""
                            for (i in page * pageSize until min(page * pageSize + pageSize, values.size)) {
                                str += values[i]!!.getString()
                            }
                            description = str
                        }
                        actionRow {
                            interactionButton(ButtonStyle.Primary, "skipToFirst") {
                                emoji = Emojis.rewind
                            }
                            interactionButton(ButtonStyle.Primary, "back") {
                                emoji = Emojis.last
                            }
                            interactionButton(ButtonStyle.Primary, "next") {
                                emoji = Emojis.next
                            }
                            interactionButton(ButtonStyle.Primary, "skipToLast") {
                                emoji = Emojis.fastforward
                            }
                        }
                        actionRow {
                            interactionButton(ButtonStyle.Secondary, "select") {
                                emoji = Emojis.numbers
                                label = "Select Page"
                            }
                            interactionButton(ButtonStyle.Danger, "close") {
                                emoji = Emojis.multiply
                                label = "Close"
                            }
                        }
                    }
                }

                button("skipToFirst") {
                    page = 0
                    update()
                }
                button("back") {
                    page--
                    if (page < 0) page = 0
                    update()
                }
                button("next") {
                    page++
                    if (page >= pages) page = pages-1
                    update()
                }
                button("skipToLast") {
                    page = pages-1
                    update()
                }
                button("select") {
                    setScreen(select)
                }
                button("close") {
                    edit {
                        content = "Pager closed."
                        embeds = mutableListOf()
                        components = mutableListOf()
                    }
                    close()
                }

                onInit(update)
            }
        }
    }
}

// Get a DiscordContext.
fun <T, C: DiscordContext<T>> CommandNode<T, C>.runs(action: suspend DiscordContext<T>.() -> Boolean) {
    executes {
        if (this !is DiscordContext<T>) return@executes false
        action()
    }
}

suspend fun <T, C : DiscordContext<T>> CommandNode<T, C>.guild(action: NodeActionParam<T, C, Snowflake?>) {
    action {
        if (this !is DiscordContext<T>) return@action null
        getGuild()?.id
    }
    int("server") {
        action {
            Snowflake(it())
        }
    }
}

suspend fun <T, C : DiscordContext<T>> CommandNode<T, C>.system(action: NodeActionParam<T, C, SystemRecord?>) {
    action {
        if (this !is DiscordContext<T>) return@action null
        database.fetchSystemFromUser(getUser())
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
