/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object FoxFetch {
    private val baseUrl = "https://api.tinyfox.dev"
    private val requestUrl = "https://api.tinyfox.dev/img?animal=fox&json"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val client = HttpClient {
        install(UserAgent) {
            agent = useragent
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun fetch() = withContext(Dispatchers.IO) {
        baseUrl + client.get(requestUrl).body<Response>().loc
    }

    data class Response(val loc: String)
}
