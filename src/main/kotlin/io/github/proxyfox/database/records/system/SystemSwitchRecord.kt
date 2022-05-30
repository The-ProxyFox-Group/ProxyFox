package io.github.proxyfox.database.records.system

// Created 2022-09-04T15:18:49

/**
 * A mutable record representing a switch
 *
 * @author KJP12
 **/
class SystemSwitchRecord {
    var systemId: String = ""
    var id: String = ""
    var memberIds: List<String> = ArrayList()
    var timestamp: String = ""
}