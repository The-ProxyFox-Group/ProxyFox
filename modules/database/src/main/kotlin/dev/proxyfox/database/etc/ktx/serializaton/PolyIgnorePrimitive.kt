/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.ktx.serializaton

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

// Created 2023-07-01T23:33:13

/**
 * @author Ampflower
 * @since ${version}
 **/
open class PolyIgnorePrimitive<T : Any>(tSerializer: KSerializer<T>) : JsonTransformingSerializer<T>(tSerializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) {
            // Null isn't valid here. Deal with it and complain to Jetbrains.
            return JsonObject(mapOf())
        }

        return element
    }
}