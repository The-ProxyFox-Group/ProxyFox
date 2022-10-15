/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.bot.parseDuration
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.stringList
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.timedYesNoPrompt
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import java.time.Instant
import kotlin.math.min

object SwitchCommands {
    suspend fun register() {
        printStep("Registering switch commands", 2)
        registerCommand(literal(arrayOf("switch", "sw"), ::empty) {
            literal(arrayOf("out", "o"), ::out)
            literal(arrayOf("move", "m"), ::moveEmpty) {
                greedy("time", ::move)
            }
            literal(arrayOf("delete", "del", "remove"), ::delete)
            literal(arrayOf("list", "l"), ::list)
            stringList("members", ::switch)
        })
    }

    private suspend fun empty(ctx: MessageHolder): String = "Make sure to provide a switch command!"

    private suspend fun out(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        database.createSwitch(system.id, listOf())
        return "Switch registered."
    }

    private suspend fun moveEmpty(ctx: MessageHolder): String = "Please provide a time to move the switch back"
    private suspend fun move(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.fetchLatestSwitch(system.id)
            ?: return "It looks like you haven't registered any switches yet"
        val oldSwitch = database.fetchSecondLatestSwitch(system.id)

        val either = ctx.params["time"]!![0].parseDuration()
        either.right?.let {
            return it
        }

        val nowMinus = Instant.now().minusMillis(either.left!!.inWholeMilliseconds)
        if (oldSwitch != null && oldSwitch.timestamp > nowMinus) {
            return "It looks like you're trying to break the space-time continuum..\n" +
                    "The provided time is set before the previous switch"
        }

        // TODO: Give more feedback to reaffirm the correct action is happening
        val message = ctx.respond("Are you sure you want to move the switch back?")
        message.timedYesNoPrompt(runner = ctx.message.author!!.id, yes = {
            switch.timestamp = nowMinus
            database.updateSwitch(switch)
            channel.createMessage("Switch updated.")
        })

        return ""
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.fetchLatestSwitch(system.id)
            ?: return "No switches registered"

        // TODO: Give more information about the switch
        val message = ctx.message.channel.createMessage("Are you sure you want to delete the latest switch?\nThe data will be lost forever (A long time!)")
        message.timedYesNoPrompt(runner = ctx.message.author!!.id, yes = {
            database.dropSwitch(switch)
            channel.createMessage("Switch deleted.")
        })

        return ""
    }

    private suspend fun EmbedBuilder.switchList(system: SystemRecord, list: List<SystemSwitchRecord>, idx: Int) {
        val append = if (system.name == null)
            "`${system.id}`"
        else "${system.name} [`${system.id}`]"
        title = "Front history of $append"

        description = buildString {
            for (i in idx * 20 until min(idx * 20 + 20, list.size)) {
                val switch = list[i]
                if (switch.memberIds.isEmpty()) {
                    append("*None*")
                } else {
                    switch.memberIds.map {
                        database.fetchMemberFromSystem(system.id, it)?.showDisplayName() ?: "*Unknown*"
                    }.joinTo(this, ", ", "**", "**")
                }
                append(" (<t:${switch.timestamp.epochSecond}:R>)\n")
            }
        }
    }

    private suspend fun list(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        // We know the system exists here, will be non-null
        val switches = database.fetchSortedSwitchesFromSystem(system.id)!!

        val idx = 0

        val message = ctx.respond {
            switchList(system, switches, idx)
        }

        // TODO: Allow cycling though switches

        return ""
    }

    private suspend fun switch(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val members = ArrayList<String>()
        var memberString = ""
        ctx.params["members"]!!.forEach {
            val member = database.findMember(system.id, it) ?: return "Couldn't find member `$it`, do they exist?"
            members += member.id
            memberString += "`${
                member.displayName
                    ?: member.name
            }`, "
        }
        memberString = memberString.substring(0, memberString.length - 2)
        database.createSwitch(system.id, members)


        return "Switch registered, current fronters: $memberString"
    }
}