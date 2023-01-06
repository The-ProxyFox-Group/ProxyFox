/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // This provides a patched Quilt Loader that can run in this environment.
    // Version is pinned, avoiding having the API suddenly break due to an
    // update that happens to effect what we depend on.
    implementation("org.quiltmc:quilt-loader:0.18.1-beta.27-20230105.235638-8")

    // Loader's dependencies; will not be required when QuiltMC/quilt-loader#190
    // is merged upstream.
    implementation("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("net.fabricmc:tiny-remapper:0.8.6")
    implementation("net.fabricmc:access-widener:2.1.0")
    implementation("org.quiltmc:quilt-json5:1.0.2")
    implementation("org.quiltmc:quilt-config:1.0.0-beta.6")
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-analysis:9.4")
    implementation("org.ow2.asm:asm-commons:9.4")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("org.ow2.asm:asm-util:9.4")

    // Bot module to avoid shadowing in Quilt Loader into ProxyFox.
    implementation(project(":modules:bot"))
}