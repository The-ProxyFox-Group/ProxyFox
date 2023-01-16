/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.interaction

import dev.kord.common.Locale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.common.entity.optional.mapList
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.json.request.ApplicationCommandCreateRequest

/**
 * The needed class is internal, so we're keeping our on impl
 * */
class ProxyFoxChatInputCreateBuilderImpl(
    override var name: String,
    override var description: String,
) : GlobalChatInputCreateBuilder {
    private val state = ProxyFoxApplicationCommandModifyStateHolder()

    override var nameLocalizations: MutableMap<Locale, String>? by state::nameLocalizations.delegate()
    override var descriptionLocalizations: MutableMap<Locale, String>? by state::descriptionLocalizations.delegate()

    override val type: ApplicationCommandType
        get() = ApplicationCommandType.ChatInput

    override var options: MutableList<OptionsBuilder>? by state::options.delegate()
    override var defaultMemberPermissions: Permissions? by state::defaultMemberPermissions.delegate()
    override var dmPermission: Boolean? by state::dmPermission.delegate()

    @Deprecated("'defaultPermission' is deprecated in favor of 'defaultMemberPermissions' and 'dmPermission'. Setting 'defaultPermission' to false can be replaced by setting 'defaultMemberPermissions' to empty Permissions and 'dmPermission' to false ('dmPermission' is only available for global commands).")
    override var defaultPermission: Boolean? by @Suppress("DEPRECATION") state::defaultPermission.delegate()


    override fun toRequest(): ApplicationCommandCreateRequest {
        return ApplicationCommandCreateRequest(
            name,
            state.nameLocalizations,
            type,
            Optional.Value(description),
            state.descriptionLocalizations,
            state.options.mapList { it.toRequest() },
            state.defaultMemberPermissions,
            state.dmPermission,
            @Suppress("DEPRECATION") state.defaultPermission,
        )
    }

}
