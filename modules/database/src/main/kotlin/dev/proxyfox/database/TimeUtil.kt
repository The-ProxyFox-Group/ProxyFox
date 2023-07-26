/*
 * Copyright (c) 2022-2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.text.ParsePosition
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Created 2022-02-10T19:21:08

/**
 * Time utilities for attempting to import older PF data.
 *
 * @author Ampflower
 * @since ${version}
 **/

private val plus = DateTimeFormatter.ofPattern("'+'")
private val spacer = DateTimeFormatter.ofPattern("[',']['-'][' ']['/']['+']")
private val dayTerminatorStrict = DateTimeFormatter.ofPattern("['st']['nd']['rd']['th']")
private val dayTerminator = DateTimeFormatter.ofPattern("['s']['t']['h']['n']['r']['d']")
private val year = DateTimeFormatterBuilder().appendValue(ChronoField.YEAR).toFormatter()

private val mmddTimezones = setOf(
    "Pacific/Honolulu",
    "America/Phoenix",
    "America/Adak",
    "America/Anchorage",
    "America/Atka",
    "America/Boise",
    "America/Chicago",
    "America/Denver",
    "America/Detroit",
    "America/Fort_Wayne",
    "America/Indianapolis",
    "America/Juneau",
    "America/Knox_IN",
    "America/Los_Angeles",
    "America/Louisville",
    "America/Menominee",
    "America/Metlakatla",
    "America/New_York",
    "America/Nome",
    "America/Shiprock",
    "America/Sitka",
    "America/Yakutat",
    "Navajo",
)

private val mmddTimezonesStartOf = setOf(
    "America/Indiana/",
    "America/Kentucky/",
    "America/North_Dakota/",
    "US/",
)

private val moy3 = mapOf(
    1L to "jan",
    2L to "feb",
    3L to "mar",
    4L to "apr",
    5L to "may",
    6L to "jun",
    7L to "jul",
    8L to "aug",
    9L to "sep",
    10L to "oct",
    11L to "nov",
    12L to "dec",
)

private val moyl = mapOf(
    1L to "january",
    2L to "february",
    3L to "march",
    4L to "april",
    5L to "may",
    6L to "june",
    7L to "july",
    8L to "august",
    9L to "september",
    10L to "october",
    11L to "november",
    12L to "december",
)

val MMMMDDuuuu = DateTimeFormatterBuilder()
    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL)
    .appendOptional(spacer)
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(dayTerminatorStrict)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter(Locale.ENGLISH)

val MMMDDuuuu = DateTimeFormatterBuilder()
    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
    .appendOptional(spacer)
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(dayTerminatorStrict)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter(Locale.ENGLISH)

val DDMMMMuuuu = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(dayTerminatorStrict)
    .appendOptional(spacer)
    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter(Locale.ENGLISH)

val DDMMMuuuu = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(dayTerminatorStrict)
    .appendOptional(spacer)
    .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter(Locale.ENGLISH)

val MMDDuuuu = DateTimeFormatterBuilder()
    .appendValue(ChronoField.MONTH_OF_YEAR)
    .appendOptional(spacer)
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter()

val DDMMuuuu = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendOptional(spacer)
    .appendValue(ChronoField.MONTH_OF_YEAR)
    .appendOptional(spacer)
    .appendOptional(year)
    .parseLenient()
    .toFormatter()

val uuuuMMDD = DateTimeFormatterBuilder()
    .appendOptional(plus)
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.NORMAL)
    .appendOptional(spacer)
    .appendValue(ChronoField.MONTH_OF_YEAR)
    .appendOptional(spacer)
    .appendValue(ChronoField.DAY_OF_MONTH)
    .parseLenient()
    .toFormatter()

val displayMonthDay = DateTimeFormatter.ofPattern("MMM dd")
val displayFull = DateTimeFormatter.ofPattern("MMM dd, uuuu")

/**
 * Attempts to parse the local date time for the following formats:
 *
 * ```java
 * December 25th, 0001
 * December 25th
 * Dec 25th, 0001
 * Dec 25th
 *
 * December 25, 0001
 * December 25
 * Dec 25, 0001
 * Dec 25
 *
 * 25th December 0001
 * 25th December
 * 25th Dec 0001
 * 25th Dec
 *
 * 25 December 0001
 * 25 December
 * 25 Dec 0001
 * 25 Dec
 *
 * 12/25/0001
 * 12-25-0001
 * 12 25 0001
 * 12/25
 * 12-25
 * 12 25
 *
 * 25/12/0001
 * 25-12-0001
 * 25 12 0001
 * 25/12
 * 25-12
 * 25 12
 *
 * 0001-12-25
 * 0001/12/25
 * 0001 12 25
 * ```
 *
 * It should be noted that the leniency offered by this method may inadvertently
 * return [MMDDuuuu] or [DDMMuuuu] due to bad user input of `0001-25-12`.
 *
 * You should only pass `false` to [preferMonthDay] when you see system strings generally returning [DDMMuuuu],
 * or the system's timezone is *not* from the US. That is, not `HT`, `AKT`, `PT`, `MT`, `CT`, `ET`,
 * or the standard or daylight-savings variants of, or the following timezones:
 * ```
 * Pacific/Honolulu
 * America/Phoenix
 * America/Adak
 * America/Anchorage
 * America/Atka
 * America/Boise
 * America/Chicago
 * America/Denver
 * America/Detroit
 * America/Fort_Wayne
 * America/Indiana/
 * America/Indianapolis
 * America/Juneau
 * America/Kentucky/
 * America/Knox_IN
 * America/Los_Angeles
 * America/Louisville
 * America/Menominee
 * America/Metlakatla
 * America/New_York
 * America/Nome
 * America/North_Dakota/
 * America/Shiprock
 * America/Sitka
 * America/Yakutat
 * Navajo
 * US/
 * ```
 *
 * If systems are returning [MMDDuuuu] and all days is `<= 12`
 *
 * @param str The date string to parse.
 * @param preferMonthDay Whether to prefer the [MMDDuuuu] format over [DDMMuuuu]. Default is true.
 * @return A pair of LocalDate and the successful formatter if parsed successfully, null otherwise.
 * @author Ampflower
 * */
fun tryParseLocalDate(str: String?, preferMonthDay: Boolean = true): Pair<LocalDate, DateTimeFormatter>? {
    // Nothing to parse.
    if (str.isNullOrBlank()) return null
    var parser = MMMMDDuuuu
    val parsed = MMMMDDuuuu.parseUnresolved(str, ParsePosition(0))
        ?: MMMDDuuuu.parseUnresolved(str, ParsePosition(0))?.also { parser = MMMDDuuuu }
        ?: DDMMMMuuuu.parseUnresolved(str, ParsePosition(0))?.also { parser = DDMMMMuuuu }
        ?: DDMMMuuuu.parseUnresolved(str, ParsePosition(0))?.also { parser = DDMMMuuuu }
        ?: uuuuMMDD.parseUnresolved(str, ParsePosition(0)).validate()?.also { parser = uuuuMMDD }
        ?: (if (preferMonthDay) MMDDuuuu else DDMMuuuu).run {
            parseUnresolved(str, ParsePosition(0))?.validate()?.also { parser = this }
        }
        ?: return null // Failed to parse
    // Fetch fields to manually construct LocalDate
    val year = if (parsed.isSupported(ChronoField.YEAR)) parsed[ChronoField.YEAR] else 1
    val month = parsed.getLong(ChronoField.MONTH_OF_YEAR).toInt()
    val day = parsed.getLong(ChronoField.DAY_OF_MONTH).toInt()
    // Construct local date
    return if (month > 12)
        LocalDate(year, day, month) to if (preferMonthDay) DDMMuuuu else MMDDuuuu
    else
        LocalDate(year, month, day) to parser
}

/**
 * Helper method to try to parse an instant if the string is not blank.
 * */
fun String?.tryParseInstant() = sanitise()?.run {
    if (isNullOrBlank()) {
        null
    } else {
        Instant.parse(this)
    }
}

fun shouldPreferMonthDay(timezone: String?) =
    timezone != null && (mmddTimezones.contains(timezone) || mmddTimezonesStartOf.any(timezone::startsWith))

fun TemporalAccessor.displayDate(): String = if (get(ChronoField.YEAR) == 1 || get(ChronoField.YEAR) == 4) {
    displayMonthDay.format(this)
} else {
    displayFull.format(this)
}

@OptIn(ExperimentalContracts::class)
fun LocalDate?.pkValid(): Boolean {
    contract {
        returns(true) implies (this@pkValid != null)
    }
    return this != null && this.year in 1..9999
}

private fun TemporalAccessor?.validate(): TemporalAccessor? {
    if (this == null) return null
    if (getLong(ChronoField.DAY_OF_MONTH) > 31) return null
    if (getLong(ChronoField.MONTH_OF_YEAR) > 31) return null
    return this
}