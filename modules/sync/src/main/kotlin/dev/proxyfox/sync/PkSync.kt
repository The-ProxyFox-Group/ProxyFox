package dev.proxyfox.sync

import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.pluralkt.PluralKt
import dev.proxyfox.pluralkt.types.PkSystem

object PkSync {
    fun pull(token: String, system: SystemRecord) {
        PluralKt.System.getMe(token) {
            if (isSuccess()) {
                getSuccess().getMembers(token)
            }
        }
    }

    private fun PkSystem.getMembers(token: String) {
        PluralKt.Member.getMembers(id, token) {
            if (isSuccess()) {
                for (member in getSuccess()) {
                    //...
                }
            }
        }
    }
}