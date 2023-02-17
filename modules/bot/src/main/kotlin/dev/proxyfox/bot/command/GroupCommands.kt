/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.subCommand
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.context.InteractionCommandContext
import dev.proxyfox.bot.command.context.runs
import dev.proxyfox.bot.deferChatInputCommand
import dev.proxyfox.bot.kordColor
import dev.proxyfox.command.NodeHolder
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.command.node.builtin.string
import dev.proxyfox.database.database
import dev.proxyfox.database.records.group.GroupRecord
import dev.proxyfox.database.records.system.SystemRecord

object GroupCommands : CommandRegistrar {
    var interactionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()

    fun SubCommandBuilder.runs(action: suspend InteractionCommandContext.() -> Boolean) {
        interactionExecutors[name] = action
    }

    override suspend fun registerSlashCommands() {
        deferChatInputCommand("group", "Manage a group") {
            subCommand("access", "View the group") {
                name()
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val group = database.findGroup(system.id, value.interaction.command.strings["name"]!!)
                    if (!checkGroup(this, group)) return@runs false
                    access(this, system, group)
                }
            }
        }
    }

    override val displayName: String = "Group"

    override suspend fun registerTextCommands() {
        Commands.parser.registerGroupCommands {
            database.fetchSystemFromUser(getUser())
        }
    }

    suspend fun <T, C : DiscordContext<T>> NodeHolder<T, C>.registerGroupCommands(getSys: suspend DiscordContext<T>.() -> SystemRecord?) {
        literal("group", "g") {
            string("group") { getGroup ->
                runs {
                    val system = getSys()
                    if (!checkSystem(this, system)) return@runs false
                    val group = database.findGroup(system.id, getGroup())
                    if (!checkGroup(this, group)) return@runs false

                    access(this, system, group)
                }
            }
        }
    }

    suspend fun <T> access(ctx: DiscordContext<T>, system: SystemRecord, group: GroupRecord): Boolean {
        val members = database.fetchMembersFromGroup(group).size
        ctx.respondEmbed {
            title = group.name
            color = group.color.kordColor()
            group.avatarUrl?.let {
                thumbnail { url = it }
            }
            group.tag?.let {
                field {
                    name = "Tag"
                    value = "$it\n**Display Mode:${group.tagMode.getDisplayString()}**"
                    inline = true
                }
            }
            field {
                name = "Members (`${members}`)"
                value = "See `pf>group ${group.id} list`"
                inline = true
            }
            group.description?.let {
                field {
                    name = "Description"
                    value = it
                }
            }
            footer {
                text =
                    "Group ID \u2009• \u2009${group.id}\u2007|\u2007System ID \u2009• \u2009${system.id}\u2007|\u2007Created "
            }
            timestamp = group.timestamp
        }

        return true
    }
}