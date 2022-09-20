/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records

// Created 2022-16-09T23:55:03

/**
 * Exception declaring a database failure of any kind, such as failure to insert it.
 *
 * @author KJP12
 * @since ${version}
 **/
class DatabaseException : RuntimeException {
    constructor()

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)
}