package io.github.proxyfox.database.records.system

// Created 2022-09-04T15:13:09

/**
 * A mutable record representing a system's server settings.
 *
 * @author KJP12
 * @since ${version}
 **/
data class SystemServerSettingsRecord(
    val systemId: String,
    var proxyEnabled: Boolean
)
