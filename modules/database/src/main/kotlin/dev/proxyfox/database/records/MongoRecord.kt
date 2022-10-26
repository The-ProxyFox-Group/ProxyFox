/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records

import org.bson.types.ObjectId

// Created 2022-26-10T15:56:22

/**
 * @author KJP12
 * @since ${version}
 **/
interface MongoRecord {
    val _id: ObjectId
}