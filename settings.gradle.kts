/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

pluginManagement {
    repositories {
        maven {
            name = "Quilt"
            url = uri("https://maven.quiltmc.org/repository/release")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "ProxyFox"

include(":modules")
include(":modules:bot")
include(":modules:common")
include(":modules:database")
include(":modules:conversion")
include(":modules:api")
include(":modules:api:server")
include(":modules:sync")