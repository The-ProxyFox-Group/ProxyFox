/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.kord.common.entity.Snowflake
import dev.proxyfox.api.AuthenticationPlugin
import dev.proxyfox.api.models.Member
import dev.proxyfox.api.models.MemberGuildSettings
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.memberRoutes() {
    route("/systems/{id}/members") {
        install(AuthenticationPlugin)
        get {
            val id = call.parameters["id"] ?: return@get call.respond("System not found")
            call.respond(database.fetchMembersFromSystem(id)?.map(Member.Companion::fromRecord) ?: emptyList())
        }

        route("/{member}") {
            get {
                val id = call.parameters["id"] ?: return@get call.respond("System not found")
                val member = database.fetchMemberFromSystem(id, call.parameters["member"]!!) ?: return@get call.respond("Member not found")
                call.respond(Member.fromRecord(member))
            }

            get("/guilds/{guild}") {
                val id = call.parameters["id"] ?: return@get call.respond("System not found")
                val member = database.fetchMemberFromSystem(id, call.parameters["member"]!!) ?: return@get call.respond("Member not found")
                val guildSettings = database.fetchMemberServerSettingsFromSystemAndMember(Snowflake(call.parameters["guild"]!!).value, id, member.id) ?: return@get call.respond("Guild not found")
                call.respond(MemberGuildSettings.fromRecord(guildSettings))
            }
        }
    }
}