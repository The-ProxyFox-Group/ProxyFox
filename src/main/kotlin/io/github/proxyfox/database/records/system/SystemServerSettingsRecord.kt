package io.github.proxyfox.database.records.system

import io.github.proxyfox.database.records.misc.AutoProxyMode
import kotlin.time.Duration

// Created 2022-09-04T15:13:09

/**
 * A mutable record representing a system's server settings.
 *
 * @author Ampflower
 * @since ${version}
 **/
data class SystemServerSettingsRecord(
    val serverId: ULong,
    val systemId: String,
    var proxyEnabled: Boolean,
    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null,
    var autoProxyMode: AutoProxyMode = AutoProxyMode.OFF,
    var autoProxyTimeout: Duration? = null
)
