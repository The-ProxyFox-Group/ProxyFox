package io.github.proxyfox.database.records.misc

class UserRecord {
    var system: String = ""
    var trust: Map<String, TrustLevel> = HashMap()
}