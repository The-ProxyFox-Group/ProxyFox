/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database.records.misc

// Created 2022-11-04T12:10:37

/**
 * Modes of the AutoProxy.
 *
 * @author Ampflower
 **/
enum class AutoProxyMode {
    /** AutoProxy is disabled; it will not automatically switch via use of tags or switch. */
    OFF,

    /**
     * Current fronter set from last proxy used
     *
     * If a proxy tag is used, the member will be automatically proxied until timeout if specified.
     * */
    LATCH,

    /**
     * Current fronter set from last switch logged
     *
     * If a switch is logged, the member will be automatically proxied until switch out.
     * */
    FRONT,

    /** AutoProxy is set to a specific member; it will not automatically switch until manually set again. */
    MEMBER,

    /** AutoProxy will fall back to the system's global default. If this is set on the system, assume [OFF].*/
    FALLBACK
}