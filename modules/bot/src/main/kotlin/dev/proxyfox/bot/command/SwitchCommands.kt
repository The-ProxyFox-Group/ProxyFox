/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.common.entity.ButtonStyle
import dev.proxyfox.bot.parseDuration
import dev.proxyfox.bot.prompts.Button
import dev.proxyfox.bot.prompts.Pager
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.stringList
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.system.SystemSwitchRecord
import java.time.Instant

object SwitchCommands {
    suspend fun register() {
        printStep("Registering switch commands", 2)
        registerCommand(literal(arrayOf("switch", "sw"), ::empty) {
            literal(arrayOf("out", "o"), ::out)
            literal(arrayOf("move", "mv", "m"), ::moveEmpty) {
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
        val author = ctx.message.author!!
        val system = database.fetchSystemFromUser(author)
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

        val members = switch.memberIds.map {
            database.fetchMemberFromSystem(system.id, it)?.showDisplayName() ?: "*Unknown*"
        }.joinToString(", ")

        TimedYesNoPrompt.build(
            runner = author.id,
            channel = ctx.message.channel,
            message = "Are you sure you want to move the switch $members back to <t:${nowMinus.epochSecond}>?",
            yes = Button("Move switch", Button.move, ButtonStyle.Primary) {
                switch.timestamp = nowMinus
                database.updateSwitch(switch)
                content = "Switch updated."
            }
        )

        return ""
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.fetchLatestSwitch(system.id)
            ?: return "No switches registered"

        val switchBefore = database.fetchSecondLatestSwitch(system.id)?.let {
            "The next latest switch is ${it.membersAsString()} (<t:${it.timestamp.epochSecond}:R>)."
        } ?: "There is no previous switch."

        val epoch = switch.timestamp.epochSecond

        TimedYesNoPrompt.build(
            runner = ctx.message.author!!.id,
            channel = ctx.message.channel,
            message = """
                Are you sure you want to delete the latest switch (${switch.membersAsString()}, <t:$epoch:R>)?
                $switchBefore
                The data will be lost forever (A long time!)
                """.trimIndent(),
            yes = Button("Delete switch", Button.wastebasket, ButtonStyle.Danger) {
                database.dropSwitch(switch)
                content = "Switch deleted."
            },
        )

        return ""
    }

    private suspend fun list(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        // We know the system exists here, will be non-null
        val switches = database.fetchSortedSwitchesFromSystem(system.id)!!

        Pager.build(ctx.message.author!!.id, ctx.message.channel, switches, 20, {
            title = "[$it] Front history of ${system.showName}"
        }, { it.membersAsString("**", "**") + " (<t:${it.timestamp.epochSecond}:R>)\n" })

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
            memberString += "`${member.showDisplayName()}`, "
        }
        memberString = memberString.substring(0, memberString.length - 2)
        database.createSwitch(system.id, members)


        return "Switch registered, current fronters: $memberString"
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