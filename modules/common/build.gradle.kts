/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.bundles.base)
    api(kotlin("stdlib"))
}

tasks.withType<ProcessResources> {
    val hash = getCommitHash()
    val branch = getBranch()
    inputs.property("hash", hash)
    inputs.property("branch", branch)
    inputs.property("version", rootProject.version)
    filesMatching("git.properties") {
        expand(
            "hash" to hash,
            "branch" to branch,
            "version" to rootProject.version
        )
    }
}

fun getCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "log", "-n", "1", "--pretty=format:\"%h\"", "--encoding=UTF-8")
        standardOutput = stdout
    }
    val str = stdout.toString(Charset.defaultCharset())
    return str.substring(1, str.length - 1)
}

fun getBranch(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString(Charset.defaultCharset())
}