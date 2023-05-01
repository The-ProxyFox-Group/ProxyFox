/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.api.routes

import dev.kord.common.entity.Snowflake
import dev.proxyfox.api.AccessPlugin
import dev.proxyfox.api.models.ApiError
import dev.proxyfox.api.models.Member
import dev.proxyfox.api.models.MemberGuildSettings
import dev.proxyfox.database.database
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.memberRoutes() {
    route("/systems/{id}/members") {
        install(AccessPlugin)
        get {
            val id = call.parameters["id"] ?: return@get call.respond(ApiError(404, "System Not Found"))
            call.respond(database.fetchMembersFromSystem(id)?.map(Member.Companion::fromRecord) ?: emptyList())
        }

        route("/{member}") {
            install(AccessPlugin)
            get {
                val id = call.parameters["id"] ?: return@get call.respond(ApiError(404, "System Not Found"))
                val member = database.fetchMemberFromSystem(id, call.parameters["member"]!!)
                        ?: return@get call.respond(ApiError(404, "Member Not Found"))
                call.respond(Member.fromRecord(member))
            }

            get("/guilds/{guild}") {
                val id = call.parameters["id"] ?: return@get call.respond(ApiError(404, "System Not Found"))
                val member = database.fetchMemberFromSystem(id, call.parameters["member"]!!)
                        ?: return@get call.respond(ApiError(404, "Member Not Found"))
                val guildSettings = database.fetchMemberServerSettingsFromSystemAndMember(Snowflake(call.parameters["guild"]!!).value, id, member.id)
                        ?: return@get call.respond(ApiError(404, "Guild Not Found"))
                call.respond(MemberGuildSettings.fromRecord(guildSettings))
            }
        }
    }
}