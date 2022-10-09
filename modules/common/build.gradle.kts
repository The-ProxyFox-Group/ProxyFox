/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import java.io.ByteArrayOutputStream
import java.nio.charset.*

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.bundles.base)
    api(kotlin("stdlib"))
}

tasks.withType<ProcessResources> {
    val hash = getCommitHash()
    inputs.property("hash", hash)
    filesMatching("commit_hash.txt") {
        expand("hash" to hash)
    }
}

fun getCommitHash(): String? {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "log", "-n", "1", "--pretty=format:\"%h\"", "--encoding=UTF-8")
        standardOutput = stdout
    }
    val str = stdout.toString(Charset.defaultCharset())
    return str.substring(1, str.length-1)
}