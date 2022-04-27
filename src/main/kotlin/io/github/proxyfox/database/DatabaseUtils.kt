package io.github.proxyfox.database

// Created 2022-11-04T14:58:16

/**
 * @author KJP12
 * @since ${version}
 **/
object DatabaseUtils {
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
}