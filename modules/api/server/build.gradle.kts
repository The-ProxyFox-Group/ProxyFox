/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

application {
    mainClass.set("dev.proxyfox.api.server.ServerMain")
}

dependencies {
    shadow(project(":modules:common"))?.let { implementation(it) }
    shadow(project(":modules:database"))?.let { implementation(it) }
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")?.let { implementation(it) }
    shadow("ch.qos.logback:logback-classic:1.2.11")?.let { implementation(it) }
    shadow("com.google.guava:guava:31.1-jre")?.let { implementation(it) }
    shadow("com.google.code.gson:gson:2.9.0")?.let { implementation(it) }
    shadow("dev.kord:kord-core:0.8.0-M16")?.let { implementation(it) }
    shadow("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.2")?.let { implementation(it) }
    shadow("org.postgresql:postgresql:42.3.3")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo:4.6.0")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo-coroutine:4.6.0")?.let { implementation(it) }
    shadow("org.litote.kmongo:kmongo-async:4.6.0")?.let { implementation(it) }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.proxyfox.api.server.ServerMainKt"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    classifier = "shadow"
    mergeServiceFiles()
}
tasks.build {
    dependsOn("shadowJar")
}