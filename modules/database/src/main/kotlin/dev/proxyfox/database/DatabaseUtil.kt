/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import com.google.gson.*
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mongodb.reactivestreams.client.MongoCollection
import dev.proxyfox.gson.LocalDateAdaptor
import dev.proxyfox.importer.ImporterException
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.filter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.util.KMongoUtil
import java.lang.reflect.RecordComponent
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// Created 2022-11-04T14:58:16

val gson = GsonBuilder()
    .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeAdaptor)
    .registerTypeAdapter(LocalDate::class.java, LocalDateAdaptor)
    .registerTypeAdapter(ObjectId::class.java, ObjectIdNullifier)
    .registerTypeAdapter(ULong::class.java, ULongAdaptor)
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

object OffsetDateTimeAdaptor : JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {
    override fun serialize(src: OffsetDateTime?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null)
            JsonNull.INSTANCE
        else
            JsonPrimitive(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(src))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OffsetDateTime? {
        return json.asString.sanitise().run {
            if (isNullOrBlank()) {
                null
            } else {
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this, OffsetDateTime::from)
            }
        }
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

object RecordAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (Record::class.java.isAssignableFrom(type.rawType)) {
            return RecordAdapter(gson, type.type, type.rawType as Class<Record>) as TypeAdapter<T>
        }
        return null
    }
}

class RecordAdapter<T : Record>(private val gson: Gson, private val type: Type, private val rawType: Class<T>) : TypeAdapter<T>() {

    private val componentMap = HashMap<String, RecordComponent>()

    init {
        assert(Record::class.java.isAssignableFrom(rawType)) { "Invalid class $rawType ($type)" }
        for (component in rawType.recordComponents) {
            componentMap[component.name] = component
        }
    }

    override fun write(out: JsonWriter, value: T) {
        out.beginObject()
        for (component in value.javaClass.recordComponents!!) {
            out.name(component.name)
            val v = component.accessor.invoke(value)
            gson.getAdapter(v.javaClass).write(out, v)
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): T {
        val list = ArrayList<Throwable>()
        val map = HashMap<String, Any?>()
        val generic = gson.getAdapter(JsonElement::class.java)

        try {
            reader.beginObject()

            while (reader.peek() == JsonToken.NAME) {
                val name = reader.nextName()
                val component = componentMap[name]

                if (component == null) {
                    val path = reader.path
                    val location = reader.toString()
                    list.add(ImporterException("Bad entry at $path: $name -> ${generic.read(reader)} @ $location"))
                } else {
                    map[name] = gson.getAdapter(TypeToken.get(`$Gson$Types`.resolve(type, rawType, componentMap[name]!!.genericType))).read(reader)
                }
            }

            reader.endObject()
        } catch (e: Throwable) {
            list.forEach(e::addSuppressed)
            throw e
        }

        if (list.isNotEmpty()) {
            val e = ImporterException("Errors encountered around ${reader.path}")
            list.forEach(e::addSuppressed)
            throw e
        }


        return rawType
            .getConstructor(*rawType.recordComponents.mapArray(RecordComponent::getType))
            .newInstance(*rawType.recordComponents.mapArray { map[it.name] })
    }

}