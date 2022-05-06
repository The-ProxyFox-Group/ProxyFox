package io.github.proxyfox.database.records.misc

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