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
import dev.kord.rest.NamedFile
import dev.kord.rest.builder.interaction.SubCommandBuilder
import dev.kord.rest.builder.interaction.subCommand
import dev.proxyfox.bot.command.MemberCommands.registerBaseMemberCommands
import dev.proxyfox.bot.command.SwitchCommands.registerSwitchCommands
import dev.proxyfox.bot.command.context.DiscordContext
import dev.proxyfox.bot.command.context.InteractionCommandContext
import dev.proxyfox.bot.command.context.runs
import dev.proxyfox.bot.command.context.system
import dev.proxyfox.bot.command.node.attachment
import dev.proxyfox.bot.hasUnixValue
import dev.proxyfox.bot.kordColor
import dev.proxyfox.bot.prompts.Button
import dev.proxyfox.bot.prompts.Pager
import dev.proxyfox.bot.prompts.TimedYesNoPrompt
import dev.proxyfox.bot.system
import dev.proxyfox.command.node.builtin.*
import dev.proxyfox.common.fromColor
import dev.proxyfox.common.printStep
import dev.proxyfox.common.toColor
import dev.proxyfox.database.database
import dev.proxyfox.database.etc.exporter.Exporter
import dev.proxyfox.database.records.system.SystemRecord
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*

/**
 * Commands for accessing and changing system settings
 * @author Oliver
 * */
object SystemCommands {
    var interactionExecutors: HashMap<String, suspend InteractionCommandContext.() -> Boolean> = hashMapOf()

    fun SubCommandBuilder.runs(action: suspend InteractionCommandContext.() -> Boolean) {
        interactionExecutors[name] = action
    }

    suspend fun Kord.registerSystemCommands() {
        createGlobalChatInputCommand("system", "Manage or create a system!") {
            subCommand("fetch", "Fetch a system card!") {
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    access(this, system!!)
                }
            }
            subCommand("create", "Create a system") {
                name(required = false)
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val name = value.interaction.command.strings["name"]
                    create(this, name)
                }
            }
            subCommand("delete", "Delete the system") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false

                    delete(this, system!!)
                }
            }
            access("system", "name") {
                name(required = false)
                raw()
                clear()
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val name = value.interaction.command.strings["name"]
                    val raw = value.interaction.command.booleans["raw"] ?: false
                    val clear = value.interaction.command.booleans["clear"] ?: false
                    name(this, system!!, name, raw, clear)
                }
            }
            subCommand("list", "List your system members") {
                system()
                bool("by-message", "Whether to sort by message count")
                bool("verbose", "Whether to display information verbosely")
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val byMessage = value.interaction.command.booleans["by-message"] ?: false
                    val verbose = value.interaction.command.booleans["verbose"] ?: false
                    list(this, system!!, byMessage, verbose)
                }
            }
            access("system", "color") {
                name("color", required = false)
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val color = value.interaction.command.strings["color"]

                    color(this, system!!, color?.toColor())
                }
            }
            access("system", "pronouns") {
                name("pronouns", required = false)
                raw()
                clear()
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val pronouns = value.interaction.command.strings["pronouns"]
                    val raw = value.interaction.command.booleans["raw"] ?: false
                    val clear = value.interaction.command.booleans["clear"] ?: false
                    pronouns(this, system!!, pronouns, raw, clear)
                }
            }
            access("system", "description") {
                system()
                name("description", required = false)
                raw()
                clear()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val desc = value.interaction.command.strings["description"]
                    val raw = value.interaction.command.booleans["raw"] ?: false
                    val clear = value.interaction.command.booleans["clear"] ?: false

                    description(this, system!!, desc, raw, clear)
                }
            }
            access("system", "avatar") {
                avatar()
                clear()
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val avatar = value.interaction.command.attachments["avatar"]?.data?.url
                    val clear = value.interaction.command.booleans["clear"] ?: false

                    avatar(this, system!!, avatar, clear)
                }
            }
            access("system", "tag") {
                name("tag", required = false)
                raw()
                clear()
                system()
                runs {
                    val system = getSystem()
                    if (!checkSystem(this, system)) return@runs false
                    val tag = value.interaction.command.strings["tag"]
                    val raw = value.interaction.command.booleans["raw"] ?: false
                    val clear = value.interaction.command.booleans["clear"] ?: false

                    tag(this, system!!, tag, raw, clear)
                }
            }
        }
    }

    suspend fun register() {
        printStep("Registering system commands", 2)
        Commands.parser.literal("list", "l") {
            runs {
                val system = database.fetchSystemFromUser(getUser())
                if (!checkSystem(this, system)) return@runs false
                list(this, system!!, false, false)
            }
            unix("params") { getParams ->
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    val params = getParams().toTypedArray()
                    val byMessage = hasUnixValue(params, "by-message-count") || hasUnixValue(params, "bmc")
                    val verbose = hasUnixValue(params, "verbose") || hasUnixValue(params, "v")
                    list(this, system!!, byMessage, verbose)
                }
            }
        }
        Commands.parser.literal("system", "sys", "s") {
            literal("new", "n", "create", "add") {
                runs {
                    create(this, null)
                }
                greedy("name") { getName ->
                    runs {
                        create(this, getName())
                    }
                }
            }
            literal("delete", "del", "remove", "rem") {
                runs {
                    val system = database.fetchSystemFromUser(getUser())
                    if (!checkSystem(this, system)) return@runs false
                    delete(this, system!!)
                }
            }
            system { getSys ->
                runs {
                    val system = getSys()
                    if (!checkSystem(this, system)) return@runs false
                    access(this, system!!)
                }
                literal("name", "rename") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        name(this, system!!, null, false, false)
                    }
                    unixLiteral("raw") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            name(this, system!!, null, true, false)
                        }
                    }
                    unixLiteral("clear", "remove") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            name(this, system!!, null, false, true)
                        }
                    }
                    greedy("name") { getName ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            name(this, system!!, getName(), false, false)
                        }
                    }
                }
                literal("list", "l") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        list(this, system!!, false, false)
                    }
                    unix("params") { getParams ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            val params = getParams().toTypedArray()
                            val byMessage = hasUnixValue(params, "by-message-count") || hasUnixValue(params, "bmc")
                            val verbose = hasUnixValue(params, "verbose") || hasUnixValue(params, "v")
                            list(this, system!!, byMessage, verbose)
                        }
                    }
                }
                literal("color", "colour", "c") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        color(this, system!!, null)
                    }
                    greedy("color") { getColor ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            color(this, system!!, getColor().toColor())
                        }
                    }
                }
                literal("pronouns", "p") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        pronouns(this, system!!, null, false, false)
                    }
                    unixLiteral("raw") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            pronouns(this, system!!, null, true, false)
                        }
                    }
                    unixLiteral("clear", "remove") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            pronouns(this, system!!, null, false, true)
                        }
                    }
                    greedy("pronouns") { getPronouns ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            pronouns(this, system!!, getPronouns(), false, false)
                        }
                    }
                }
                literal("description", "desc", "d") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        description(this, system!!, null, false, false)
                    }
                    unixLiteral("raw") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            description(this, system!!, null, true, false)
                        }
                    }
                    unixLiteral("clear", "remove") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            description(this, system!!, null, false, true)
                        }
                    }
                    greedy("description") { getDesc ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            description(this, system!!, getDesc(), false, false)
                        }
                    }
                }
                literal("avatar", "pfp") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        avatar(this, system!!, null, false)
                    }
                    unixLiteral("clear", "remove") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            avatar(this, system!!, null, true)
                        }
                    }
                    attachment("avatar") { getAvatar ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            avatar(this, system!!, getAvatar().url, false)
                        }
                    }
                    string("avatar") { getAvatar ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            avatar(this, system!!, getAvatar(), false)
                        }
                    }
                }
                literal("tag", "t") {
                    runs {
                        val system = getSys()
                        if (!checkSystem(this, system)) return@runs false
                        tag(this, system!!, null, false, false)
                    }
                    unixLiteral("raw") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            tag(this, system!!, null, true, false)
                        }
                    }
                    unixLiteral("clear", "remove") {
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            tag(this, system!!, null, false, true)
                        }
                    }
                    greedy("description") { getTag ->
                        runs {
                            val system = getSys()
                            if (!checkSystem(this, system)) return@runs false
                            tag(this, system!!, getTag(), false, false)
                        }
                    }
                }

                registerBaseMemberCommands(getSys)
                registerSwitchCommands(getSys)
            }
        }
    }

    private suspend fun <T> access(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        val members = database.fetchTotalMembersFromSystem(system.id)
        ctx.respondEmbed {
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
            timestamp = system.timestamp
        }
        return true
    }

    private suspend fun <T> create(ctx: DiscordContext<T>, name: String?): Boolean {
        val system = database.getOrCreateSystem(ctx.getUser()!!)
        system.name = name
        database.updateSystem(system)
        val add = if (name != null) " with name $name" else ""
        ctx.respondSuccess("System created$add! See `pf>help` or `/info help` for how to set up your system further.")
        return true
    }

    private suspend fun <T> name(ctx: DiscordContext<T>, system: SystemRecord, name: String?, raw: Boolean, clear: Boolean): Boolean {
        if (clear) {
            if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
                ctx.respondFailure("You don't have access to edit this information.")
                return false
            }

            system.name = null
            database.updateSystem(system)
            ctx.respondSuccess("System name cleared!")
        }

        name ?: run {
            system.name ?: run {
                ctx.respondFailure("System doesn't have a name set.")
                return false
            }

            if (raw)
                ctx.respondPlain("`${system.name}`")
            else ctx.respondSuccess("System's name is ${system.name}")
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.name = name
        database.updateSystem(system)
        ctx.respondSuccess("System name updated to ${system.name}!")
        return true
    }

    private suspend fun <T> list(ctx: DiscordContext<T>, system: SystemRecord, byMessage: Boolean, verbose: Boolean): Boolean {
        // TODO: List by message

        if (verbose) {
            ctx.respondEmbed {
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
            return true
        }

        val proxies = database.fetchProxiesFromSystem(system.id)!!
        Pager.build(
            ctx.getUser()!!.id,
            ctx.getChannel(),
            database.fetchMembersFromSystem(system.id)!!.map { m -> m to proxies.filter { it.memberId == m.id } },
            20,
            { page -> system(system, nameTransformer = { "[$page] Members of $it" }) },
            {
                val str = if (it.second.isNotEmpty()) it.second.joinToString("\uFEFF``, ``\uFEFF", " (``\uFEFF", "\uFEFF``)") else ""
                "`${it.first.id}`\u2007•\u2007**${it.first.name}**${str}\n"
            },
        )
        return true
    }

    suspend fun <T> color(ctx: DiscordContext<T>, system: SystemRecord, color: Int?): Boolean {
        color ?: run {
            ctx.respondSuccess("Member's color is `${system.color.fromColor()}`")
            return true
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.color = color
        database.updateSystem(system)
        ctx.respondSuccess("Member's color is now `${color.fromColor()}!")
        return true
    }

    private suspend fun <T> pronouns(ctx: DiscordContext<T>, system: SystemRecord, pronouns: String?, raw: Boolean, clear: Boolean): Boolean {
        if (clear) {
            if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
                ctx.respondFailure("You don't have access to edit this information.")
                return false
            }

            system.pronouns = null
            database.updateSystem(system)
            ctx.respondSuccess("System pronouns cleared!")
            return true
        }

        pronouns ?: run {
            system.pronouns ?: run {
                ctx.respondFailure("System doesn't have pronouns set")
                return false
            }

            if (raw) {
                ctx.respondPlain("`${system.pronouns}`")
                return true
            }

            ctx.respondSuccess("System's pronouns are ${system.pronouns}")
            return true
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.pronouns = pronouns
        database.updateSystem(system)
        ctx.respondSuccess("System pronouns updated to $pronouns!")
        return true
    }

    suspend fun <T> description(ctx: DiscordContext<T>, system: SystemRecord, description: String?, raw: Boolean, clear: Boolean): Boolean {
        if (clear) {
            if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
                ctx.respondFailure("You don't have access to edit this information.")
                return false
            }

            system.description = null
            database.updateSystem(system)
            ctx.respondSuccess("System's description cleared!")
            return true
        }

        description ?: run {
            system.description ?: run {
                ctx.respondWarning("System has no description set")
                return true
            }

            if (raw)
                ctx.respondPlain("```md\n${system.description}```")
            else ctx.respondSuccess("System's description is ${system.description}")

            return true
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.description = description
        database.updateSystem(system)
        ctx.respondSuccess("System description updated!")

        return true
    }

    suspend fun <T> avatar(ctx: DiscordContext<T>, system: SystemRecord, avatar: String?, clear: Boolean): Boolean {
        if (clear) {
            if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
                ctx.respondFailure("You don't have access to edit this information.")
                return false
            }

            system.avatarUrl = null
            database.updateSystem(system)
            ctx.respondSuccess("System's avatar cleared!")
            return true
        }

        avatar ?: run {
            system.avatarUrl ?: run {
                ctx.respondWarning("Member doesn't have an avatar set.")
                return true
            }

            ctx.respondEmbed {
                image = system.avatarUrl
                color = system.color.kordColor()
            }
            return true
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.avatarUrl = avatar
        database.updateSystem(system)
        ctx.respondSuccess("Member's avatar updated!")

        return true
    }

    private suspend fun <T> tag(ctx: DiscordContext<T>, system: SystemRecord, tag: String?, raw: Boolean, clear: Boolean): Boolean {
        if (clear) {
            if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
                ctx.respondFailure("You don't have access to edit this information.")
                return false
            }
            system.tag = null
            database.updateSystem(system)
            ctx.respondSuccess("System tag cleared!")
            return true
        }

        tag ?: run {
            system.tag ?: run {
                ctx.respondFailure("System doesn't have a tag set.")
                return false
            }

            if (raw) {
                ctx.respondPlain("`${system.tag}`")
                return true
            }

            ctx.respondSuccess("System's tag is ${system.tag}")

            return true
        }

        if (!system.hasFullAccess(ctx.getUser()!!.id.value)) {
            ctx.respondFailure("You don't have access to edit this information.")
            return false
        }

        system.tag = tag
        database.updateSystem(system)
        ctx.respondSuccess("System tag updated to $tag!")
        return true
    }

    private suspend fun <T> delete(ctx: DiscordContext<T>, system: SystemRecord): Boolean {
        TimedYesNoPrompt.build(
            runner = ctx.getUser()!!.id,
            channel = ctx.getChannel(),
            message = "Are you sure you want to delete your system?\n" +
                    "The data will be lost forever (A long time!)",
            yes = Button("Delete system", Button.wastebasket, ButtonStyle.Danger) {
                val export = Exporter.export(ctx.getUser()!!.id.value)
                ctx.respondFiles(null, NamedFile("system.json", ChannelProvider { export.byteInputStream().toByteReadChannel() }))
                database.dropSystem(ctx.getUser()!!)
                content = "System deleted."
            },
        )
        return true
    }
}