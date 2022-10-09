/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.exporter

import dev.proxyfox.database.DatabaseTestUtil.offsetDateTimeEpoch
import dev.proxyfox.database.DatabaseTestUtil.offsetDateTimeEpochString
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import dev.proxyfox.types.PkMember
import dev.proxyfox.types.PkSwitch
import dev.proxyfox.types.PkSystem
import org.testng.Assert
import org.testng.annotations.Test

// Created 2022-09-10T01:26:00

/**
 * @author KJP12
 * @since ${version}
 **/
class ExporterTest {
    @Test
    fun `Exporter(System) - retain seconds`() {
        val system = PkSystem(SystemRecord().apply {
            timestamp = offsetDateTimeEpoch
        })
        Assert.assertEquals(system.created, offsetDateTimeEpochString)
    }

    @Test
    fun `Exporter(Member) - retain seconds`() {
        val member = PkMember(MemberRecord().apply {
            timestamp = offsetDateTimeEpoch
        }, null)
        Assert.assertEquals(member.created, offsetDateTimeEpochString)
    }

    @Test
    fun `Exporter(Switch) - retain seconds`() {
        val switch = PkSwitch(SystemSwitchRecord().apply {
            timestamp = offsetDateTimeEpoch
        })
        Assert.assertEquals(switch.timestamp, offsetDateTimeEpochString)
    }
}