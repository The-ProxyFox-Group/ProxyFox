/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.timedYesNoPrompt
import dev.proxyfox.common.printStep
import dev.proxyfox.database.database
import java.time.OffsetDateTime

object SwitchCommands {
    suspend fun register() {
        printStep("Registering switch commands", 2)
        registerCommand(literal(arrayOf("switch", "sw"), ::empty) {
            literal("out", ::out)
            literal("move", ::moveEmpty) {
                greedy("time", ::move)
            }
            literal(arrayOf("delete", "del", "d", "remove", "rem"), ::delete)
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

        TODO()
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
                ?: return "System does not exist. Create one using `pf>system new`"
        val switch = database.getLatestSwitch(system.id)
                ?: return "No switches registered"

        // TODO: Give more information about the switch
        val message1 = ctx.message.channel.createMessage("Are you sure you want to delete the latest switch?\nThe data will be lost forever (A long time!)")
        message1.timedYesNoPrompt(runner = ctx.message.author!!.id, yes = {
            database.removeSwitch(switch)
            channel.createMessage("Switch deleted")
        })

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