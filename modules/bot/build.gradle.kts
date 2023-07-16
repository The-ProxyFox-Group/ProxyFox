/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:database"))
    implementation(project(":modules:api"))
    implementation(project(":modules:sync"))
}

application.mainClass.set("dev.proxyfox.bot.BotMainKt")

tasks {
    shadowJar {
        archiveBaseName.set("proxyfox")
        archiveClassifier.set("")
        mergeServiceFiles()
    }
}