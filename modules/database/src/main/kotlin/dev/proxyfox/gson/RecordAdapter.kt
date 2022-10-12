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
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import dev.proxyfox.database.mapArray
import dev.proxyfox.importer.ImporterException
import org.slf4j.LoggerFactory
import java.lang.reflect.RecordComponent
import java.lang.reflect.Type
import kotlin.reflect.full.primaryConstructor

class RecordAdapter<T : Record>(private val gson: Gson, private val type: Type, private val rawType: Class<T>) : TypeAdapter<T>() {

    private val componentMap = HashMap<String, RecordComponent>()
    private val serialisedName = HashMap<RecordComponent, String>()

    init {
        assert(Record::class.java.isAssignableFrom(rawType)) { "Invalid class $rawType ($type)" }
        for (component in rawType.recordComponents) {
            componentMap[component.name] = component

            rawType.getDeclaredField(component.name).getAnnotation(SerializedName::class.java)?.let {
                componentMap[it.value] = component
                serialisedName[component] = it.value
                for (alt in it.alternate) componentMap[alt] = component
            }
        }
    }

    override fun write(out: JsonWriter, value: T) {
        out.beginObject()
        for (component in value.javaClass.recordComponents!!) {
            out.name(serialisedName[component] ?: component.name)
            component.accessor.invoke(value)?.also {
                gson.getAdapter(it.javaClass).write(out, it)
            } ?: out.nullValue()
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): T {
        val list = ArrayList<Throwable>()
        val map = HashMap<String, Any?>()
        val generic = gson.getAdapter(JsonElement::class.java)

        try {
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                @Suppress("UNCHECKED_CAST")
                val primitiveProcessor = rawType.getAnnotation(UnexpectedValueProcessor::class.java) as? UnexpectedValueProcessor<T>
                    ?: throw ImporterException("Unable to import $rawType @ $reader as token is ${reader.peek()}")

                val obj = primitiveProcessor.value.objectInstance ?: primitiveProcessor.value.primaryConstructor?.call()
                ?: throw ImporterException("Unable to import $rawType @ $reader as token is ${reader.peek()} and $primitiveProcessor returned an unconstructable class ${primitiveProcessor.value}")
                return when (reader.peek()) {
                    JsonToken.BEGIN_ARRAY -> obj.ifArray(reader)
                    JsonToken.STRING -> obj.ifString(reader)
                    JsonToken.BOOLEAN -> obj.ifBoolean(reader)
                    JsonToken.NUMBER -> obj.ifNumber(reader)
                    else -> throw ImporterException("Unable to import $rawType @ $reader as token is ${reader.peek()}")
                }
            } else {
                reader.beginObject()

                while (reader.peek() == JsonToken.NAME) {
                    val name = reader.nextName()
                    val component = componentMap[name]
                    val path = reader.path

                    if (component == null) {
                        val output = generic.read(reader)
                        if (output != null && !output.isJsonNull && !(output.isJsonArray && output.asJsonArray.isEmpty) && !(output.isJsonObject && output.asJsonObject.size() == 0)) {
                            val location = reader.toString()
                            list.add(ImporterException("Bad entry at $path: $name -> $output @ $location"))
                        }
                    } else try {
                        map[name] = gson.getAdapter(TypeToken.get(`$Gson$Types`.resolve(type, rawType, componentMap[name]!!.genericType))).read(reader)
                    } catch (e: Exception) {
                        throw ImporterException("Unexpected exception processing $component @ $path - ${reader.path}", e)
                    }
                }

                reader.endObject()
            }
        } catch (e: Throwable) {
            list.forEach(e::addSuppressed)
            throw e
        }

        if (list.isNotEmpty()) {
            val e = ImporterException("Errors encountered around ${reader.previousPath} - ${reader.path}")
            list.forEach(e::addSuppressed)
            logger.warn("Record reader traces", e)
        }


        return rawType
            .getDeclaredConstructor(*rawType.recordComponents.mapArray(RecordComponent::getType))
            .newInstance(*rawType.recordComponents.mapArray { map[it.name] })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RecordAdapter::class.java)
    }
}