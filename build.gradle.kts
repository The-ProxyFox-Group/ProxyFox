/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

tasks {
    jar {
        archiveClassifier.set("nodeps")
    }
    shadowJar {
        archiveClassifier.set("")
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
        }
        findByName("shadowJar")?.let {
            build {
                dependsOn(it)
            }
        }
    }
}