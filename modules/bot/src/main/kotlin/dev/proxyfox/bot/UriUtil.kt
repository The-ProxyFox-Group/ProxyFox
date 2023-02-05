/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.bot

import java.net.URI
import java.net.URISyntaxException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * @author Ampflower
 * @since ${version}
 **/

const val mascot = "https://cdn.discordapp.com/emojis/981031355079065601.png"
const val invalidAvatar = "https://fakeimg.pl/256x256/000000/?text=Invalid+Avatar"
const val exampleExport = "https://cdn.discordapp.com/attachments/962064545923354634/1071663479678828634/example-export.json"

val validLinkSchemes = setOf("http", "https")

val linkWrapper = mapOf(
    '\'' to '\'',
    '`' to '`',
    '"' to '"',
    '<' to '>',
    '[' to ']',
    '{' to '}',
    '(' to ')',
    '「' to '」',
    '『' to '』',
    '“' to '”',
)

// The following two are required due to imports and the fact that the database already contains
// invalid entries. Kinda annoying, but not much better without rewriting the database.

fun String?.httpUri(): String? = this?.httpUri()

/**
 * Returns a URI known to be compliant with Discord's constraints, else returning the default.
 *
 * Note: The TLD is required by Discord, so links such as `https://example` and `https://example.`
 * are considered invalid while `https://example.com` and `https://example.com.` are considered valid.
 *
 * @receiver The URI to validate
 * @param default The default URL if the URI is invalid.
 * @return The URI, maybe unwrapped if wrapped in input, else the default if invalid or missing a TLD.
 * */
fun String.httpUri(default: String = invalidAvatar): String = uri().let {
    if (it.isValidHttpUrl()) it.toString() else default
}

fun String?.httpUriOrNull(): String? = uri().let {
    if (it.isValidHttpUrl()) it.toString() else null
}

fun String?.uri(): URI? {
    // Account for Windows/DOS paths.
    // Note that this is the only path that's allowed to be special-cased for `\`.
    if (this != null && this[0].isLetter() && this[1] == ':') {
        return this.replace('\\', '/').uri()
    }

    return this?.uri()
}

@JvmName("uriInternal")
private fun String.uri(): URI? {
    return if (isNullOrBlank()) null else try {
        linkWrapper[this[0]]?.let {
            if (!endsWith(it) || length == 2) {
                null
            } else {
                URI(substring(1, length - 1))
            }
        } ?: URI(this)
    } catch (ignore: URISyntaxException) {
        null
    }
}

@OptIn(ExperimentalContracts::class)
fun URI?.isValidHttpUrl(): Boolean {
    contract {
        returns(true) implies (this@isValidHttpUrl != null)
    }
    return this != null && scheme !in validLinkSchemes && hasValidHost
}

val URI.hasValidHost: Boolean
    get() = host.containsTld()

private fun String?.containsTld() = this != null && lastIndexOf('.', lastIndex - 1) >= 0

fun URI?.isFile(): Boolean {
    return this != null && (scheme == "file" || (host == null && (scheme == null || scheme.length == 1 && scheme[0].isLetter())))
}

fun URI?.invalidUrlMessage(commandBase: String, example: String = mascot): String? {
    if (isFile()) {
        return "You tried linking to a file, did you mean to upload it instead?"
    }

    if (this?.scheme == null) {
        return invalidUrlGeneric(commandBase, example)
    }

    if (scheme !in validLinkSchemes) {
        return invalidUrlScheme(commandBase, scheme, example)
    }

    if (!hasValidHost) {
        return invalidUrlHost(commandBase, host, example)
    }

    return null
}

fun invalidUrlGeneric(commandBase: String, example: String = mascot): String {
    return "Invalid URL. Be sure to use `pf>$commandBase <link>`.\n\nExample: `pf>$commandBase $example`"
}

fun invalidUrlScheme(commandBase: String, scheme: String?, example: String = mascot): String {
    return "Invalid protocol `$scheme`. Be sure to use HTTP or HTTPS.\n\nExample: `pf>$commandBase $example`"
}

fun invalidUrlHost(commandBase: String, host: String?, example: String = mascot): String {
    return "Invalid host `$host`. Are you using an actual domain?\n\nExample: `pf>$commandBase $example`"
}