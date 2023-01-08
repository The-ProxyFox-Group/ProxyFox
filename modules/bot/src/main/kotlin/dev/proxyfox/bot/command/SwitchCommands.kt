/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.context.InteractionCommandContext
import dev.proxyfox.bot.command.context.runs
import dev.proxyfox.bot.parseDuration
import dev.proxyfox.bot.prompts.Button
import dev.proxyfox.bot.prompts.Pager
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.command.node.builtin.greedy
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.command.node.builtin.stringList
import dev.proxyfox.common.printStep
import dev.proxyfox.common.trimEach
import dev.proxyfox.database.database
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

object SwitchCommands {
    var interactionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()

    fun SubCommandBuilder.runs(action: suspend InteractionCommandContext.() -> Boolean) {
        interactionExecutors[name] = action
    }

    suspend fun Kord.registerSwitchCommands() {
        createGlobalChatInputCommand("switch", "Create or manage switches!") {
            subCommand("create", "Create a switch") {
                string("members", "The members to use, comma separated") {
                    required = true
                }
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val members = value.interaction.command.strings["members"]!!.split(",").toTypedArray()
                    members.trimEach()
                    switch(this, system!!, members)
                }
            }
            subCommand("out", "Marks that no-one's fronting") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    out(this, system!!)
                }
            }
            subCommand("delete", "Deletes the latest switch") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val switch = database.fetchLatestSwitch(system!!.id)
                    if (!checkSwitch(this, switch)) return@runs false
                    val oldSwitch = database.fetchSecondLatestSwitch(system.id)
                    delete(this, system, switch!!, oldSwitch)
                }
            }
            subCommand("move", "Moves the latest switch") {
                name("time")
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val switch = database.fetchLatestSwitch(system!!.id)
                    if (!checkSwitch(this, switch)) return@runs false
                    val oldSwitch = database.fetchSecondLatestSwitch(system.id)
                    val time = value.interaction.command.strings["time"]!!
                    move(this, system, switch!!, oldSwitch, time)
                }
            }
            subCommand("list", "Lists your switches") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    list(this, system!!)
                }
            }
        }
    }

    suspend fun register() {
        printStep("Registering switch commands", 2)
        Commands.parser.literal("switch", "sw") {
            runs {
                respondFailure("Please provide a switch subcommand.")
                false
            }
            literal("out", "o") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    out(this, system!!)
                }
            }
            literal("delete", "del", "remove", "rem") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val switch = database.fetchLatestSwitch(system!!.id)
                    if (!checkSwitch(this, switch)) return@runs false
                    val oldSwitch = database.fetchSecondLatestSwitch(system.id)
                    delete(this, system, switch!!, oldSwitch)
                }
            }
            literal("move","mv","m") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val switch = database.fetchLatestSwitch(system!!.id)
                    if (!checkSwitch(this, switch)) return@runs false
                    val oldSwitch = database.fetchSecondLatestSwitch(system.id)
                    move(this, system, switch!!, oldSwitch, null)
                }
                greedy("time") { getTime ->
                    runs {
                        val system = database.fetchSystemFromUser(getUser())
                        if (!checkSystem(this, system)) return@runs false
                        val switch = database.fetchLatestSwitch(system!!.id)
                        if (!checkSwitch(this, switch)) return@runs false
                        val oldSwitch = database.fetchSecondLatestSwitch(system.id)
                        move(this, system, switch!!, oldSwitch, getTime())
                    }
                }
            }
            literal("list", "l") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    list(this, system!!)
                }
            }
            stringList("members") { getMembers ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    switch(this, system!!, getMembers().toTypedArray())
                }
            }
        }
    }

    private suspend fun <T> out(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        database.createSwitch(system.id, listOf())
        ctx.respondSuccess("Switch registered. Take care!")
        return true
    }

    private suspend fun <T> move(ctx: DiscordContext<T>, system: SystemRecord, switch: SystemSwitchRecord, oldSwitch: SystemSwitchRecord?, time: String?): Boolean {
        time ?: run {
            ctx.respondFailure("Please provide a time to move the switch back")
            return false
        }

        val oldSwitch = database.fetchSecondLatestSwitch(system.id)

        val either = time.parseDuration()
        either.right?.let {
            ctx.respondFailure(it)
            return false
        }

        val nowMinus = Clock.System.now().minus(either.left!!.inWholeMilliseconds, DateTimeUnit.MILLISECOND)
        if (oldSwitch != null && oldSwitch.timestamp > nowMinus) {
            ctx.respondFailure("It looks like you're trying to break the space-time continuum..\n" +
                    "The provided time is set before the previous switch")
            return false
        }

        val members = switch.memberIds.map {
            database.fetchMemberFromSystem(system.id, it)?.showDisplayName() ?: "*Unknown*"
        }.joinToString(", ")

        TimedYesNoPrompt.build(
            runner = ctx.getUser()!!.id,
            channel = ctx.getChannel(),
            message = "Are you sure you want to move the switch $members back to <t:${nowMinus.epochSeconds}>?",
            yes = Button("Move switch", Button.move, ButtonStyle.Primary) {
                switch.timestamp = nowMinus
                database.updateSwitch(switch)
                content = "Switch updated."
            }
        )

        return true
    }

    private suspend fun <T> delete(ctx: DiscordContext<T>, system: SystemRecord, switch: SystemSwitchRecord, oldSwitch: SystemSwitchRecord?): Boolean {
        val epoch = switch.timestamp.epochSeconds

        TimedYesNoPrompt.build(
            runner = ctx.getUser()!!.id,
            channel = ctx.getChannel(),
            message = """
                Are you sure you want to delete the latest switch (${switch.membersAsString()}, <t:$epoch:R>)? ${if (oldSwitch != null) "\nThe previous switch would be at <t:${oldSwitch.timestamp.epochSeconds}:R>" else ""}
                The data will be lost forever (A long time!)
                """.trimIndent(),
            yes = Button("Delete switch", Button.wastebasket, ButtonStyle.Danger) {
                database.dropSwitch(switch)
                content = "Switch deleted."
            },
        )

        return true
    }

    private suspend fun <T> list(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        // We know the system exists here, will be non-null
        val switches = database.fetchSortedSwitchesFromSystem(system.id)!!

        Pager.build(ctx.getUser()!!.id, ctx.getChannel(), switches, 20, {
            title = "[$it] Front history of ${system.showName}"
        }, { it.membersAsString("**", "**") + " (<t:${it.timestamp.epochSeconds}:R>)\n" })

        return true
    }

    private suspend fun <T> switch(ctx: DiscordContext<T>, system: SystemRecord, members: Array<String>): Boolean {
        val membersOut = ArrayList<String>()
        var memberString = ""
        members.forEach {
            val member = database.findMember(system.id, it) ?: run {
                ctx.respondFailure("Couldn't find member `$it`, do they exist?")
                return false
            }
            membersOut += member.id
            memberString += "`${member.showDisplayName()}`, "
        }
        memberString = memberString.substring(0, memberString.length - 2)
        database.createSwitch(system.id, membersOut)

        ctx.respondSuccess("Switch registered! Current fronters: $memberString")
        return true
    }

    private suspend fun SystemSwitchRecord.membersAsString(prefix: String = "", postfix: String = ""): String {
        return if (memberIds.isEmpty()) {
            "*None*"
        } else {
            memberIds.map {
                database.fetchMemberFromSystem(systemId, it)?.showDisplayName() ?: "*Unknown*"
            }.joinToString(", ", prefix, postfix)
        }
    }
}