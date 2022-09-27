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
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.filter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.util.KMongoUtil
import java.lang.reflect.Type
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Created 2022-11-04T14:58:16

val gson = GsonBuilder()
    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdaptor)
    .registerTypeAdapter(ObjectId::class.java, ObjectIdNullifier)
    .registerTypeAdapter(ULong::class.java, ULongAdaptor)
    .create()!!

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

object OffsetDateTimeAdaptor : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    override fun serialize(src: OffsetDateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null)
            JsonNull.INSTANCE
        else
            JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(src))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OffsetDateTime {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(json.asString, OffsetDateTime::from)
    }
}

object ObjectIdNullifier : JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {
    override fun serialize(src: ObjectId?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ObjectId {
        return ObjectId()
    }
}

object ULongAdaptor : JsonSerializer<ULong>, JsonDeserializer<ULong> {
    override fun serialize(src: ULong?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src.toLong())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): ULong {
        return json.asLong.toULong()
    }
}