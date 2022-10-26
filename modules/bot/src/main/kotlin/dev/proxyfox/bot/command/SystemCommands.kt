/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.NamedFile
import dev.proxyfox.bot.kordColor
import dev.proxyfox.bot.prompts.Button
import dev.proxyfox.bot.prompts.Pager
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.unixLiteral
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.bot.system
import dev.proxyfox.bot.toKtInstant
import dev.proxyfox.common.fromColor
import dev.proxyfox.common.printStep
import dev.proxyfox.common.toColor
import dev.proxyfox.database.database
import dev.proxyfox.exporter.Exporter

/**
 * Commands for accessing and changing system settings
 * @author Oliver
 * */
object SystemCommands {
    suspend fun register() {
        printStep("Registering system commands", 2)
        registerCommand(literal(arrayOf("system", "s"), ::empty) {
            literal(arrayOf("new", "n", "create", "add"), ::createEmpty) {
                greedy("name", ::create)
            }

            literal(arrayOf("name", "rename"), ::accessName) {
                greedy("name", ::rename)
            }

            literal(arrayOf("list", "l"), ::list) {
                unixLiteral(arrayOf("by-message-count", "bmc"), ::listByMessage)
                unixLiteral(arrayOf("verbose", "v"), ::listVerbose)
            }

            literal(arrayOf("color", "colour"), ::colorEmpty) {
                greedy("color", ::color)
            }

            literal(arrayOf("pronouns", "p"), ::pronounsEmpty) {
                unixLiteral("raw", ::pronounsRaw)
                greedy("pronouns", ::pronouns)
            }

            literal(arrayOf("description", "desc", "d"), ::descriptionEmpty) {
                unixLiteral("raw", ::descriptionRaw)
                greedy("desc", ::description)
            }

            literal(arrayOf("avatar", "pfp"), ::avatarEmpty) {
                unixLiteral("raw", ::avatarRaw)
                unixLiteral("clear", ::avatarClear)
                unixLiteral("delete", ::avatarClear)
                greedy("avatar", ::avatar)
            }

            literal("tag", ::tagEmpty) {
                unixLiteral("raw", ::tagRaw)
                unixLiteral("clear", ::tagClear)
                unixLiteral("delete", ::tagClear)
                greedy("tag", ::tag)
            }

            literal(arrayOf("delete", "del", "remove"), ::delete)
        })

        registerCommand(literal(arrayOf("list", "l"), ::list) {
            unixLiteral(arrayOf("by-message-count", "bmc"), ::listByMessage)
            unixLiteral(arrayOf("verbose", "v"), ::listVerbose)
        })
    }

    private suspend fun empty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val members = database.fetchTotalMembersFromUser(ctx.message.author)
        ctx.respond {
            title = system.name ?: system.id
            color = system.color.kordColor()
            system.avatarUrl?.let {
                thumbnail { url = it }
            }
            system.tag?.let {
                field {
                    name = "Tag"
                    value = it
                    inline = true
                }
            }
            system.pronouns?.let {
                field {
                    name = "Pronouns"
                    value = it
                    inline = true
                }
            }
            field {
                name = "Members (`${members}`)"
                value = "See `pf>system list`"
                inline = true
            }
            system.description?.let {
                field {
                    name = "Description"
                    value = it
                }
            }
            footer {
                text = "ID \u2009• \u2009${system.id}\u2007|\u2007Created "
            }
            timestamp = system.timestamp.toKtInstant()
        }
        return ""
    }

    private suspend fun createEmpty(ctx: MessageHolder): String {
        database.getOrCreateSystem(ctx.message.author!!)
        return "System created! See `pf>help` for how to set up your system further!"
    }

    private suspend fun create(ctx: MessageHolder): String {
        val system = database.getOrCreateSystem(ctx.message.author!!)
        system.name = ctx.params["name"]!![0]
        database.updateSystem(system)
        return "System created with name ${system.name}! See `pf>help` for how to set up your system further!"
    }

    private suspend fun renameEmpty(ctx: MessageHolder): String {
        database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to provide me with a name to update your system!"
    }

    private suspend fun rename(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.name = ctx.params["name"]!![0]
        database.updateSystem(system)
        return "System name updated to ${system.name}!"
    }

    private suspend fun accessName(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "System's name is ${system.name}"
    }

    private suspend fun list(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val proxies = database.fetchProxiesFromSystem(system.id)!!
        Pager.build(
            ctx.message.author!!.id,
            ctx.message.channel,
            database.fetchMembersFromSystem(system.id)!!.map { m -> m to proxies.filter { it.memberId == m.id } },
            20,
            { page -> system(system, nameTransformer = { "[$page] Members of $it" }) },
            {
                val str = if (it.second.isNotEmpty()) it.second.joinToString("\uFEFF``, ``\uFEFF", " (``\uFEFF", "\uFEFF``)") else ""
                "`${it.first.id}`\u2007•\u2007**${it.first.name}**${str}\n"
            },
        )
        return ""
    }

    private suspend fun listByMessage(ctx: MessageHolder): String {
        // TODO: Make it sort by message count
        return list(ctx)
    }

    private suspend fun listVerbose(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        ctx.respond {
            system(system, nameTransformer = { "Members of $it" })
            val proxies = database.fetchProxiesFromSystem(system.id)
            for (m in database.fetchMembersFromSystem(system.id)!!) {
                val memberProxies = proxies?.filter { it.memberId == m.id }
                field {
                    name = "${m.asString()} [`${m.id}`]"
                    value = if (memberProxies.isNullOrEmpty()) "*No proxy tags set.*" else memberProxies.joinToString("\uFEFF``\n``\uFEFF", "``\uFEFF", "\uFEFF``")
                    inline = true
                }
            }
        }
        return ""
    }

    private suspend fun colorEmpty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.color.fromColor()?.let { "System's color is `$it` " } ?: "There's no color set."

    }

    private suspend fun color(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.color = ctx.params["color"]!![0].toColor()
        database.updateSystem(system)
        return "Member's color updated!"
    }

    private suspend fun pronouns(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.pronouns = ctx.params["pronouns"]!![0]
        database.updateSystem(system)
        return "Pronouns updated!"
    }

    private suspend fun pronounsRaw(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.pronouns?.let { "``$it``" } ?: "There's no pronouns set."
    }

    private suspend fun pronounsEmpty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.pronouns?.let { "System's pronouns are set to $it" } ?: "There's no pronouns set."
    }

    private suspend fun description(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.description = ctx.params["desc"]!![0]
        database.updateSystem(system)
        return "Description updated!"
    }

    private suspend fun descriptionRaw(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.description?.let { "```md\n$it```" } ?: "There's no description set."
    }

    private suspend fun descriptionEmpty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.description ?: "Description not set."
    }

    private suspend fun avatar(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.avatarUrl = ctx.params["avatar"]!![0]
        database.updateSystem(system)
        return "System avatar updated!"
    }

    private suspend fun avatarClear(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.avatarUrl = null
        database.updateSystem(system)
        return "System avatar cleared!"
    }

    private suspend fun avatarRaw(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.avatarUrl}`"
    }

    private suspend fun avatarEmpty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.avatarUrl ?: "System avatar not set."
    }

    private suspend fun tag(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.tag = ctx.params["tag"]!![0]
        database.updateSystem(system)
        return "System tag updated!"
    }

    private suspend fun tagClear(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        system.tag = null
        database.updateSystem(system)
        return "System tag cleared!"
    }

    private suspend fun tagRaw(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "`${system.tag}`"
    }

    private suspend fun tagEmpty(ctx: MessageHolder): String {
        val system = database.fetchSystemFromUser(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return system.tag ?: "System tag not set."
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val author = ctx.message.author!!
        database.fetchSystemFromUser(author)
            ?: return "System does not exist. Create one using `pf>system new`"

        TimedYesNoPrompt.build(
            runner = author.id,
            channel = ctx.message.channel,
            message = "Are you sure you want to delete your system?\n" +
                    "The data will be lost forever (A long time!)",
            yes = Button("Delete system", Button.wastebasket, ButtonStyle.Danger) {
                val export = Exporter.export(author.id.value)
                ctx.sendFiles(NamedFile("system.json", export.byteInputStream()))
                database.dropSystem(author)
                content = "System deleted."
            },
        )
        return ""
    }
}