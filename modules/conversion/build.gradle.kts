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
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("dev.proxyfox.conversion.ConversionMainKt")
}

dependencies {
    implementation(project(":modules:common"))
    implementation(project(":modules:database"))
}

tasks {
    shadowJar {
        archiveClassifier.set("shadow")
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}
