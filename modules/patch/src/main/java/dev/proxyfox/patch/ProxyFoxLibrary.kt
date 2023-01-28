/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch

import net.fabricmc.api.EnvType

// Created 2023-03-01T02:42:57
/**
 * @author Ampflower
 * @since 2.0.8
 */
enum class ProxyFoxLibrary : Library {
    PROXY_FOX("dev/proxyfox/bot/BotMainKt.class"),
    QUILT_LIBRARIES(
        "org/quiltmc/loader/impl/launch/knot/Knot.class",
        "net/fabricmc/loader/launch/server/FabricServerLauncher.class",
        "org/quiltmc/json5/JsonReader.class",
        "org/quiltmc/config/api/Config.class",
        "org/objectweb/asm/util/ASMifier.class",
        "org/objectweb/asm/tree/ClassNode.class",
        "org/objectweb/asm/commons/JSRInlinerAdapter.class",
        "org/objectweb/asm/tree/analysis/Frame.class",
        "org/objectweb/asm/Opcodes.class",
        "net/fabricmc/accesswidener/AccessWidener.class",
        "org/spongepowered/asm/mixin/Debug.class",
        "net/fabricmc/tinyremapper/AsmRemapper.class",
        "net/fabricmc/mapping/tree/TinyTree.class"
    );

    private val envType: EnvType?
    private val paths: List<String>

    constructor(envType: EnvType, vararg paths: String) {
        this.envType = envType
        this.paths = java.util.List.of(*paths)
    }

    constructor(vararg paths: String) {
        envType = null
        this.paths = java.util.List.of(*paths)
    }

    override fun envType(): EnvType? {
        return envType
    }

    override fun paths(): List<String> {
        return paths
    }
}