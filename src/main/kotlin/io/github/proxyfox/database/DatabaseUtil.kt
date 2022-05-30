package io.github.proxyfox.database

import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.reactivestreams.filter
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.util.KMongoUtil

// Created 2022-11-04T14:58:16

/**
 * @author Ampflower
 * @since ${version}
 **/
object DatabaseUtil {
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

    suspend inline fun <T> KCollection<T>.findOne(filter: String): T? = find().filter(filter).awaitFirstOrNull()
    suspend inline fun <T> KCollection<T>.findAll(filter: String): List<T> = find().filter(filter).toList()
    suspend inline fun <reified T : Any> Mongo.getOrCreateCollection(): MongoCollection<T> {
        if (listCollectionNames().toList().indexOf(KMongoUtil.defaultCollectionName(T::class)) == -1)
            try {
                createCollection(KMongoUtil.defaultCollectionName(T::class)).awaitFirst()
            } catch (e: Throwable) {
            }
        return getCollection()
    }
}