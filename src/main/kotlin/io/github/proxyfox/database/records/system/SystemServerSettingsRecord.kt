package io.github.proxyfox.database.records.system

import io.github.proxyfox.database.records.misc.AutoProxyMode

// Created 2022-09-04T15:13:09

/**
 * A mutable record representing a system's server settings.
 *
 * @author KJP12
 * @since ${version}
 **/
class SystemServerSettingsRecord {
    var serverId: String = ""
    var systemId: String = ""
    var proxyEnabled: Boolean = true

    /** The ID of the member that's currently being auto-proxied. */
    var autoProxy: String? = null
    var autoProxyMode: AutoProxyMode = AutoProxyMode.OFF
}