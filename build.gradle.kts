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
    alias(libs.plugins.licenser)
}

tasks {
    jar {
        archiveClassifier.set("nodeps")
    }
    shadowJar {
        archiveClassifier.set("")
    }
    register<Copy>("poolRelease") {
        for (proj in subprojects) {
            val shadowJar = proj.tasks.findByPath("shadowJar")
            if (shadowJar != null) {
                from(shadowJar)
            }
        }
        into(project.buildDir.resolve("pool"))
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.quiltmc.gradle.licenser")

    license {
        rule(file("${rootProject.projectDir}/HEADER"))
        include("**/*.kt")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")
        maven("https://maven.proxyfox.dev")
    }

    dependencies {
        testImplementation(rootProject.libs.bundles.test)
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = java.targetCompatibility.majorVersion
            kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
        }
        findByName("shadowJar")?.let {
            build {
                dependsOn(it)
            }
        }
        test {
            useTestNG()
        }
    }
}