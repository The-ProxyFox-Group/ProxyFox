package io.github.proxyfox.database.records.misc

import dev.kord.common.entity.Snowflake

// Created 2022-10-04T21:06:30

/**
 * @author KJP12
 * @since ${version}
 **/
class ServerSettingsRecord {
    var serverId: String = ""
    var proxyRole: String? = null
    var disabledChannels: List<Snowflake>? = null
}