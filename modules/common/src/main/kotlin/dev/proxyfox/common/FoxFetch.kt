/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.common

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.*

object FoxFetch {
    private val baseUrl = "https://api.tinyfox.dev"
    private val url = URL("https://api.tinyfox.dev/img?animal=fox&json")

    suspend fun fetch()  = withContext(Dispatchers.IO) {
        baseUrl + Json.parseToJsonElement(url.readText()).jsonObject["loc"]!!.jsonPrimitive.content
    }
}
