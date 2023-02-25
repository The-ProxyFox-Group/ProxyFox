/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot.terminal

import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.menu.CommandMenu
import dev.proxyfox.common.logger

class TerminalContext(override val command: String) : CommandContext<String>() {
    override val value: String = command

    override suspend fun menu(action: suspend CommandMenu.() -> Unit) {

    }

    override suspend fun respondFailure(text: String, private: Boolean): String {
        logger.error(text)
        return text
    }

    override suspend fun respondPlain(text: String, private: Boolean): String {
        logger.info(text)
        return text
    }

    override suspend fun respondSuccess(text: String, private: Boolean): String {
        logger.info(text)
        return text
    }

    override suspend fun respondWarning(text: String, private: Boolean): String {
        logger.warn(text)
        return text
    }
}