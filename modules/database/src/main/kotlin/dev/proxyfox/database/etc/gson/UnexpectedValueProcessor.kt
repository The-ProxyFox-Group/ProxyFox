/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.etc.gson

import kotlin.reflect.KClass

// Created 2022-01-10T03:36:52

/**
 * @author Ampflower
 * @since ${version}
 **/
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class UnexpectedValueProcessor<T>(
    val value: KClass<out ValueProcessor<out T>>
)
