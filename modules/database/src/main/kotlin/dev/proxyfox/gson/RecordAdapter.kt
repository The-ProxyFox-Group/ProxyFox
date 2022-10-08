/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.proxyfox.database.mapArray
import dev.proxyfox.importer.ImporterException
import java.lang.reflect.RecordComponent
import java.lang.reflect.Type

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