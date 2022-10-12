/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.google.gson.*
import com.mongodb.reactivestreams.client.MongoCollection
import dev.proxyfox.gson.*
import dev.proxyfox.importer.ImporterException
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.filter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.util.KMongoUtil
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Created 2022-11-04T14:58:16

const val pkIdBound = 11881376

val gson = GsonBuilder()
    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdaptor)
    .registerTypeAdapter(LocalDate::class.java, LocalDateAdaptor)
    .registerTypeAdapter(ObjectId::class.java, ObjectIdNullifier)
    .registerTypeAdapter(Instant::class.java, InstantAdaptor)
    .registerTypeAdapter(ULong::class.java, ULongAdaptor)
    .registerTypeAdapter(Void::class.java, VoidAdaptor)
    .registerTypeAdapterFactory(RecordAdapterFactory)
    .create()!!

fun String.sanitise(): String {
    return replace("\u0000", "").trim()
}

@JvmName("sanitiseNullable")
fun String?.sanitise(): String? {
    return this?.sanitise()
}

fun String?.validate(name: String? = "Unknown field"): String {
    return sanitise().apply { if (isNullOrBlank()) throw ImporterException("Invalid string given for $name: \"$this\"") }!!
}

fun Int.toPkString(): String {
    val arr = CharArray(5)
    var tmp = this
    var i = 0
    while (tmp > 0) {
        arr[i] = ((tmp % 26) + 'a'.code).toChar()
        i++
        tmp /= 26
    }
    val slice = arr.sliceArray(0 until i)
    slice.reverse()
    return "a".repeat(5 - i) + String(slice)
}

fun String.fromPkString(): Int {
    var tmp = 0
    var i = 0
    while (i < length) {
        tmp = (tmp * 26) + (this[i] - 'a')
        i++
    }
    return tmp
}

fun Int.paddedString(zeros: Int) = toString().padStart(zeros, '0')

@OptIn(ExperimentalContracts::class)
fun String?.isValidPkString(): Boolean {
    contract {
        returns(true) implies (this@isValidPkString != null)
    }
    return !isNullOrBlank() && length == 5 && chars().allMatch { it >= 'a'.code && it <= 'z'.code }
}

/**
 * A rather cursed way to find first free, but it's kinda
 * hard to come up with anything good here.
 *
 * @receiver The collection of PK-compatible IDs to find the first free of.
 * @return The first free ID as integer.
 * */
fun Collection<String>.firstFreeRaw(): Int {
    var newId = size
    for ((index, id) in map { it.fromPkString() }.sorted().withIndex()) {
        if (index != id) {
            newId = index
            break
        }
    }
    return newId
}

/**
 * A rather cursed way to find first free, but it's kinda
 * hard to come up with anything good here.
 *
 * @receiver The collection of PK-compatible IDs to find the first free of.
 * @return The first free ID as string.
 * */
fun Collection<String>.firstFree(): String {
    return firstFreeRaw().toPkString()
}

fun databaseFromString(db: String?) =
    when (db) {
        "nop" -> NopDatabase()
        "json" -> JsonDatabase()
        "postgres" -> TODO("Postgres db isn't implemented yet!")
        "mongo", null -> MongoDatabase()
        else -> throw IllegalArgumentException("Unknown database $db")
    }

inline fun unsupported(message: String = "Not implemented"): Nothing = throw UnsupportedOperationException(message)

inline fun <T, reified R> Array<out T>.mapArray(action: (T) -> R): Array<R> {
    return Array(size) { action(this[it]) }
}

suspend inline fun <T> KCollection<T>.findOne(filter: String): T? = find().filter(filter).awaitFirstOrNull()
suspend inline fun <T> KCollection<T>.findAll(filter: String): List<T> = find().filter(filter).toList()
suspend inline fun <reified T : Any> Mongo.getOrCreateCollection(): MongoCollection<T> {
    if (listCollectionNames().toList().indexOf(KMongoUtil.defaultCollectionName(T::class)) == -1)
        try {
            createCollection(KMongoUtil.defaultCollectionName(T::class)).awaitFirst()
        } catch (ignored: Throwable) {
        }
    return getCollection()
}
