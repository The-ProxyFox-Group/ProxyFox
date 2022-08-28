package dev.proxyfox.bot.command

import dev.kord.common.Color
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.create.embed
import dev.proxyfox.bot.kord
import dev.proxyfox.bot.string.dsl.greedy
import dev.proxyfox.bot.string.dsl.literal
import dev.proxyfox.bot.string.dsl.string
import dev.proxyfox.bot.string.parser.MessageHolder
import dev.proxyfox.bot.string.parser.registerCommand
import dev.proxyfox.common.fromColor
import dev.proxyfox.common.printStep
import dev.proxyfox.common.toColor
import dev.proxyfox.database.database
import kotlinx.coroutines.Job

/**
 * Commands for accessing and changing system  settings
 * @author Oliver
 * */
object MemberCommands {
    suspend fun register() {
        printStep("Registering  commands", 2)
        val commands: CommandNode = {
            string("member", ::access) {
                val name: CommandNode = {
                    greedy("name", ::rename)
                }
                literal("rename", ::renameEmpty, name)
                literal("name", ::renameEmpty, name)

                val nickname: CommandNode = {
                    literal("--clear", ::nicknameClear)
                    greedy("name", ::nickname)
                }
                literal("nickname", ::nicknameEmpty, nickname)
                literal("nick", ::nicknameEmpty, nickname)
                literal("displayname", ::nicknameEmpty, nickname)
                literal("dn", ::nicknameEmpty, nickname)

                val servername: CommandNode = {
                    literal("--clear", ::servernameClear)
                    greedy("name", ::servername)
                }
                literal("servername", ::servernameEmpty, servername)
                literal("servernick", ::servernameEmpty, servername)

                val desc: CommandNode = {
                    literal("--clear", ::descriptionClear)
                    literal("--raw", ::descriptionRaw)
                    greedy("desc", ::description)
                }
                literal("desc", ::descriptionEmpty, desc)
                literal("description", ::descriptionEmpty, desc)
                literal("d", ::descriptionEmpty, desc)

                val avatar: CommandNode = {
                    literal("--clear", ::avatarClear)
                    greedy("avatar", ::avatarLinked)
                }
                literal("avatar", ::avatar, avatar)
                literal("pfp", ::avatar, avatar)

                val serveravatar: CommandNode = {
                    literal("--clear", ::serverAvatarClear)
                    greedy("avatar", ::serverAvatarLinked)
                }
                literal("serveravatar", ::serverAvatar, serveravatar)
                literal("serverpfp", ::serverAvatar, serveravatar)

                literal("proxy", ::proxyEmpty) {
                    literal("remove", ::removeProxyEmpty) {
                        greedy("proxy", ::removeProxy)
                    }
                    literal("add", ::proxyEmpty) {
                        greedy("proxy", ::proxy)
                    }
                    greedy("proxy", ::proxy)
                }

                literal("pronouns", ::pronounsEmpty) {
                    literal("--clear", ::pronounsClear)
                    literal("--raw", ::pronounsRaw)
                    greedy("pronouns", ::pronouns)
                }

                literal("color", ::colorEmpty) {
                    greedy("color", ::color)
                }

                literal("birthday", ::birthEmpty) {
                    literal("--clear", ::birthClear)
                    greedy("birthday", ::birth)
                }

                literal("delete", ::delete)
                literal("remove", ::delete)
            }

            literal("delete", ::deleteEmpty) {
                greedy("member", ::delete)
            }
            literal("remove", ::deleteEmpty) {
                greedy("member", ::delete)
            }
            literal("new", ::createEmpty) {
                greedy("name", ::create)
            }
            literal("n", ::createEmpty) {
                greedy("name", ::create)
            }
            literal("create", ::createEmpty) {
                greedy("name", ::create)
            }

        }
        registerCommand(literal("member", ::empty, commands))
        registerCommand(literal("m", ::empty, commands))
    }

    private fun empty(ctx: MessageHolder): String {
        return "Make sure to provide a member command!"
    }

    private suspend fun access(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        ctx.message.channel.createMessage {
            embed {
                title = "${member.name} [`${member.id}`]"
                if (member.displayName != null) title = "${member.displayName} (${member.name}) [`${member.id}`]"
                color = Color(member.color)
                val desc = member.description
                if (desc != null) field {
                    name = "Description"
                    value = desc
                    inline = false
                }
                val pronouns = member.pronouns
                if (pronouns != null) field {
                    name = "Pronouns"
                    value = pronouns
                    inline = true
                }
                val birthday = member.birthday
                if (birthday != null) field {
                    name = "Birthday"
                    value = birthday
                    inline = true
                }
            }
        }
        return ""
    }

    private suspend fun renameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return "Member's name is `${member.name}`"
    }

    private suspend fun rename(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.name = ctx.params["name"]!![0]
        database.updateMember(member)
        return "Updated member's name!"
    }

    private suspend fun nicknameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.displayName != null)
            "Member's display name is `${member.displayName}`"
        else "Member doesn't have a display name."
    }

    private suspend fun nickname(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.displayName = ctx.params["name"]?.get(0)
        database.updateMember(member)
        return "Member displayname updated!"
    }

    private suspend fun nicknameClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.displayName = null
        database.updateMember(member)
        return "Member displayname cleared!"
    }

    private suspend fun servernameEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        return if (serverMember.nickname != null)
            "Member's server nickname is `${serverMember.nickname}`"
        else "Member doesn't have a server nickname"
    }

    private suspend fun servername(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        serverMember.nickname = ctx.params["name"]?.get(0)
        database.updateMemberServerSettings(serverMember)
        return "Member's server nickname updated!"
    }

    private suspend fun servernameClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        serverMember.nickname = null
        database.updateMemberServerSettings(serverMember)
        return "Member's server nickname removed!"
    }

    private suspend fun descriptionEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.description != null)
            "Member's description is ${member.description}"
        else "Member doesn't have a description"
    }

    private suspend fun descriptionRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.description != null)
            "`${member.description}`"
        else "Member doesn't have a description"
    }

    private suspend fun description(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.description = ctx.params["desc"]?.get(0)
        database.updateMember(member)
        return "Member description updated!"
    }

    private suspend fun descriptionClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.description = null
        database.updateMember(member)
        return "Member description cleared!"
    }

    private suspend fun avatarLinked(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.avatarUrl = ctx.params["avatar"]?.get(0)
        database.updateMember(member)
        return "Member avatar updated!"
    }

    private suspend fun avatar(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val attachments = ctx.message.attachments
        if (attachments.isEmpty())
            return if (member.avatarUrl != null)
                member.avatarUrl!!
            else "Member doesn't have an avatar"
        member.avatarUrl = attachments.first().url
        database.updateMember(member)
        return "Member avatar updated!"
    }

    private suspend fun avatarClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.avatarUrl = null
        database.updateMember(member)
        return "Member avatar cleared!"
    }

    private suspend fun serverAvatarLinked(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        serverMember.avatarUrl = ctx.params["avatar"]?.get(0)
        database.updateMemberServerSettings(serverMember)
        return "Member server avatar updated!"
    }

    private suspend fun serverAvatar(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        val attachments = ctx.message.attachments
        if (attachments.isEmpty())
            return if (serverMember.avatarUrl != null)
                serverMember.avatarUrl!!
            else "Member doesn't have a server avatar"
        serverMember.avatarUrl = attachments.first().url
        database.updateMemberServerSettings(serverMember)
        return "Member server avatar updated!"
    }

    private suspend fun serverAvatarClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val serverMember =
            database.getMemberServerSettingsById(ctx.message.getGuild(), system.id, member.id)!!
        serverMember.avatarUrl = null
        database.updateMemberServerSettings(serverMember)
        return "Member server avatar cleared!"
    }

    private suspend fun removeProxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return "Please provide a proxy tag to remove"
    }

    private suspend fun removeProxy(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val proxy = ctx.params["proxy"]!![0]
        val proxyTag = database.getProxyTagFromMessage(ctx.message.author, proxy)
            ?: return "Proxy tag doesn't exist in this member"
        if (proxyTag.memberId != member.id) return "Proxy tag doens't exist in this member"
        database.removeProxyTag(proxyTag)
        return "Proxy removed."
    }

    private suspend fun proxyEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return "Please provide a subcommand or a proxy tag"
    }

    private suspend fun proxy(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        val proxy = ctx.params["proxy"]!![0]
        if (!proxy.contains("text")) return "Given proxy tag does not contain `text`"
        val prefix = proxy.substring(0, proxy.indexOf("text"))
        val suffix = proxy.substring(4 + prefix.length, proxy.length)
        if (prefix.isEmpty() && suffix.isEmpty()) return "Proxy tag must contain either a prefix or a suffix"
        database.allocateProxyTag(system.id, member.id, prefix, suffix)
            ?: return "Proxy tag already exists in this system"
        return "Proxy tag created!"
    }

    private suspend fun pronounsEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.pronouns == null)
            "Member does not have pronouns set"
        else "Member's pronouns are ${member.pronouns}"
    }

    private suspend fun pronounsRaw(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.pronouns == null)
            "Member does not have pronouns set"
        else "`${member.pronouns}`"
    }

    private suspend fun pronouns(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.pronouns = ctx.params["pronouns"]?.get(0)
        database.updateMember(member)
        return "Member's pronouns updated!"
    }

    private suspend fun pronounsClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.pronouns = null
        database.updateMember(member)
        return "Member's pronouns cleared!"
    }

    private suspend fun colorEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return "Member's color is `${member.color.fromColor()}`"

    }

    private suspend fun color(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.color = ctx.params["color"]!![0].toColor()
        database.updateMember(member)
        return "Member's color updated!"
    }

    private suspend fun birthEmpty(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        return if (member.birthday == null)
            "Member does not have a birthday reigstered"
        else "Member's birthday is ${member.birthday}"
    }

    private suspend fun birth(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.birthday = ctx.params["birthday"]?.get(0)
        database.updateMember(member)
        return "Member's birthday updated!"
    }

    private suspend fun birthClear(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"
        member.birthday = null
        database.updateMember(member)
        return "Member's birthday cleared!"
    }

    private suspend fun delete(ctx: MessageHolder): String {
        val author = ctx.message.author!!
        val channel = ctx.message.channel
        val system = database.getSystemByHost(author)
            ?: return "System does not exist. Create one using `pf>system new`"
        val member = database.findMember(system.id, ctx.params["member"]!![0])
            ?: return "Member does not exist. Create one using `pf>member new`"

        val message1 = channel.createMessage("Are you sure you want to delete member `${member.displayName ?: member.name}`?\nTheir data will be lost forever (A long time!)")
        message1.addReaction(ReactionEmoji.Unicode("❌"))
        message1.addReaction(ReactionEmoji.Unicode("✅"))
        var job: Job? = null
        job = kord.on<ReactionAddEvent> {
            if (messageId == message1.id && userId == author.id) {
                when (emoji.name) {
                    "✅" -> {
                        database.removeMember(system.id, member.id)
                        channel.createMessage("Member deleted")
                        job!!.cancel()
                    }
                    "❌" -> {
                        channel.createMessage("Action cancelled")
                        job!!.cancel()
                    }
                }
            }
        }

        return ""
    }

    private suspend fun deleteEmpty(ctx: MessageHolder): String {
        database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to tell me which member you want to delete!"
    }

    private suspend fun createEmpty(ctx: MessageHolder): String {
        database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        return "Make sure to provide a name for the new member!"
    }

    private suspend fun create(ctx: MessageHolder): String {
        val system = database.getSystemByHost(ctx.message.author)
            ?: return "System does not exist. Create one using `pf>system new`"
        database.allocateMember(system.id, ctx.params["name"]!![0])
        return "Member created!"
    }
}
