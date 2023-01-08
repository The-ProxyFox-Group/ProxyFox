/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.exporter

import dev.proxyfox.database.DatabaseTestUtil.instantEpoch
import dev.proxyfox.database.DatabaseTestUtil.instantLastMicroOfEpochDay
import dev.proxyfox.database.DatabaseTestUtil.stringEpoch
import dev.proxyfox.database.DatabaseTestUtil.stringLastMicroOfEpochDay
import dev.proxyfox.database.etc.types.PkMember
import dev.proxyfox.database.etc.types.PkSwitch
import dev.proxyfox.database.etc.types.PkSystem
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import org.testng.Assert
import org.testng.annotations.Test

// Created 2022-09-10T01:26:00

/**
 * @author Ampflower
 * @since ${version}
 **/
class ExporterTest {
    @Test
    fun `Exporter(System) - retain seconds`() {
        val system = PkSystem(SystemRecord().apply {
            timestamp = instantEpoch
        })
        Assert.assertEquals(system.created, stringEpoch)
    }

    @Test
    fun `Exporter(Member) - retain seconds`() {
        val member = PkMember(MemberRecord().apply {
            timestamp = instantEpoch
        }, null)
        Assert.assertEquals(member.created, stringEpoch)
    }

    @Test
    fun `Exporter(Switch) - retain seconds`() {
        val switch = PkSwitch(SystemSwitchRecord().apply {
            timestamp = instantEpoch
        })
        Assert.assertEquals(switch.timestamp, stringEpoch)
    }

    @Test
    fun `Exporter(Switch) - retain microseconds`() {
        val switch = PkSwitch(SystemSwitchRecord().apply {
            timestamp = instantLastMicroOfEpochDay
        })
        Assert.assertEquals(switch.timestamp, stringLastMicroOfEpochDay)
    }
}