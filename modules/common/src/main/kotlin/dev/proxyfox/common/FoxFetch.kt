package dev.proxyfox.common

import com.google.gson.*
import kotlinx.coroutines.*
import java.net.*

object FoxFetch {
    private val baseUrl = "https://api.tinyfox.dev"
    private val url = URL("https://api.tinyfox.dev/img?animal=fox&json")

    private class FoxRecord {
        var loc: String = ""
    }

    suspend fun fetch()  = withContext(Dispatchers.IO) {
        baseUrl + url.openStream().reader().use { JsonParser.parseReader(it).asJsonObject["loc"].asString }
    }
}