/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.command.interaction

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.rest.builder.interaction.OptionsBuilder
import kotlinx.serialization.SerialName

/**
 * The needed class is internal, so we're keeping our on impl
 * */
class ProxyFoxApplicationCommandModifyStateHolder {

    var name: Optional<String> = Optional.Missing()
    var nameLocalizations: Optional<MutableMap<Locale, String>?> = Optional.Missing()

    var description: Optional<String> = Optional.Missing()
    var descriptionLocalizations: Optional<MutableMap<Locale, String>?> = Optional.Missing()

    var options: Optional<MutableList<OptionsBuilder>> = Optional.Missing()

    var defaultMemberPermissions: Optional<Permissions?> = Optional.Missing()
    var dmPermission: OptionalBoolean? = OptionalBoolean.Missing


    @Deprecated("'defaultPermission' is deprecated in favor of 'defaultMemberPermissions' and 'dmPermission'. Setting 'defaultPermission' to false can be replaced by setting 'defaultMemberPermissions' to empty Permissions and 'dmPermission' to false ('dmPermission' is only available for global commands).")
    @SerialName("default_permission")
    var defaultPermission: OptionalBoolean = OptionalBoolean.Missing
}
