package io.github.proxyfox.database.records.misc

class UserRecord {
    var id: String = ""
    var system: String? = null
    var trust: Map<String, TrustLevel> = HashMap()
}