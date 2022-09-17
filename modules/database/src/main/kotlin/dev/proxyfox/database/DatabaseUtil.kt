/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.filter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.util.KMongoUtil
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Created 2022-11-04T14:58:16

fun Int.toPkString(): String {
    val arr = CharArray(5)
    var tmp = this
    var i = 0
    while (tmp > 0) {
        arr[i] = ((tmp % 26) + 'a'.code).toChar()
        i++
        tmp /= 26
    }
    return "a".repeat(5 - i) + String(arr.sliceArray(0 until i))
}

fun String.fromPkString(): Int {
    var tmp = 0
    var i = 0
    while (i < length) {
        tmp += this[i] - 'a'
        i++
    }
    return tmp
}

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
 * @return The first free ID.
 * */
fun Collection<String>.firstFree(): String {
    var newId = size
    for ((index, id) in map { it.fromPkString() }.sorted().withIndex()) {
        if (index != id) {
            newId = index
            break
        }
    }
    return newId.toPkString()
}

fun databaseFromString(db: String?) =
    when (db) {
        "nop" -> NopDatabase()
        "json" -> JsonDatabase()
        "postgres" -> TODO("Postgres db isn't implemented yet!")
        "mongo", null -> MongoDatabase()
        else -> throw IllegalArgumentException("Unknown database $db")
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