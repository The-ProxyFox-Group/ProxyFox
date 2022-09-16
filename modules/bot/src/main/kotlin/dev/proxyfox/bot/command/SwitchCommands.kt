/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.rest.builder.message.EmbedBuilder
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.timedYesNoPrompt
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import org.litote.kmongo.minute
import java.time.OffsetDateTime
import java.time.temporal.TemporalUnit
import kotlin.math.floor

object SwitchCommands {
    suspend fun register() {
        printStep("Registering switch commands", 2)
        registerCommand(literal(arrayOf("switch", "sw"), ::empty) {
            literal(arrayOf("out", "o"), ::out)
            literal(arrayOf("move", "m"), ::moveEmpty) {
                greedy("time", ::move)
            }
            literal(arrayOf("delete", "del", "d", "remove", "rem"), ::delete)
            literal(arrayOf("list", "l"), ::list)
            greedy("members", ::switch)
        })
    }

    private suspend fun empty(ctx: MessageHolder): String = "Make sure to provide a switch command!"

    private suspend fun out(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
                ?: return "System does not exist. Create one using `pf>system new`"
        database.allocateSwitch(system.id, listOf(), OffsetDateTime.now())
        return "Switch registered."
    }

    private suspend fun moveEmpty(ctx: MessageHolder): String = "Please provide a time to move the switch back"
    private suspend fun move(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
                ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.getLatestSwitch(system.id)
                ?: return "It looks like you haven't registered any switches yet"
        val oldSwitch = database.getSecondLatestSwitch(system.id)
        val time = ctx.params["time"]!![0].split(Regex("\\s")).joinToString("")

        var years = -1L
        var weeks = -1L
        var days = -1L
        var hours = -1L
        var minutes = -1L
        var seconds = -1L

        var currNum = ""

        for (i in time) {
            if (i.isDigit()) currNum += i
            else when (i) {
                'y' -> {
                    if (years == -1L) years = currNum.toLong()
                    else return "Provided time string contains multiple year declarations."
                }
                'w' -> {
                    if (weeks == -1L) weeks = currNum.toLong()
                    else return "Provided time string contains multiple week declarations."
                }
                'd' -> {
                    if (days == -1L) days = currNum.toLong()
                    else return "Provided time string contains multiple day declarations."
                }
                'h' -> {
                    if (hours == -1L) hours = currNum.toLong()
                    else return "Provided time string contains multiple hour declarations."
                }
                'm' -> {
                    if (minutes == -1L) minutes = currNum.toLong()
                    else return "Provided time string contains multiple minute declarations."
                }
                's' -> {
                    if (seconds == -1L) seconds = currNum.toLong()
                    else return "Provided time string contains multiple second declarations."
                }
            }
        }

        if (seconds == -1L) seconds = 0
        if (minutes == -1L) minutes = 0
        if (hours == -1L) hours = 0
        if (days == -1L) days = 0
        if (weeks == -1L) weeks = 0
        if (years == -1L) years = 0

        if (seconds >= 60) {
            minutes += floor(seconds/60f).toInt()
            seconds %= 60
        }

        if (minutes >= 60) {
            hours += floor(minutes/60f).toInt()
            minutes %= 60
        }

        if (hours >= 24) {
            days += floor(hours/24f).toInt()
            hours %= 24
        }

        days += weeks*7

        if (days >= 365) {
            years += floor(days/365f).toInt()
            days %= 7
        }

        val totalSeconds = ((((years * 365 + days) * 24 + hours) * 60 + minutes) * 60) + seconds
        val nowMinus = OffsetDateTime.now().minusSeconds(totalSeconds)
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
        val system = database.getSystemByHost(ctx.message.author)
                ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.getLatestSwitch(system.id)
                ?: return "No switches registered"

        // TODO: Give more information about the switch
        val message = ctx.message.channel.createMessage("Are you sure you want to delete the latest switch?\nThe data will be lost forever (A long time!)")
        message.timedYesNoPrompt(runner = ctx.message.author!!.id, yes = {
            database.removeSwitch(switch)
            channel.createMessage("Switch deleted.")
        })

        return ""
    }

    private suspend fun EmbedBuilder.switchList(system: SystemRecord, list: List<SystemSwitchRecord>, idx: Int) {
        val append = if (system.name == null)
            "`${system.id}`"
        else "${system.name} [`${system.id}`]"
        title = "Front history of $append"

        var descString = ""
        for (i in idx*20 until idx*20+20) {
            if (i > list.size) break
            val switch = list[i]
            val members = switch.memberIds.mapNotNull {
                val member = database.getMemberById(system.id, it)
                member?.displayName ?: member?.name
            }


        }
    }

    private suspend fun list(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        // We know the system exists here, will be non-null
        val switches = database.getSortedSwitchesById(system.id)!!

        val idx = 0

        val message = ctx.respond {
            switchList(system, switches, idx)
        }

        // TODO: Allow cycling though switches

        return ""
    }

    private suspend fun switch(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
                ?: return "System does not exist. Create one using `pf>system new`"
        val members = ArrayList<String>()
        var memberString = ""
        ctx.params["members"]?.forEach {
            val member = database.getMemberByIdAndName(system.id, it)
                    ?: database.getMemberById(system.id, it)
                    ?: return "Couldn't find member `$it`, do they exist?"
            members += member.id
            memberString += "`${
                member.displayName
                        ?: member.name
            }`, "
        }
        memberString = memberString.substring(0, memberString.length-2)
        database.allocateSwitch(system.id, ctx.params["members"]!!.asList(), OffsetDateTime.now())


        return "Switch registered, current fronters: $memberString"
    }
}