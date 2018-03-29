package com.azure.data.util.json

import com.azure.core.util.RoundtripDateConverter
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.azure.data.model.*
import java.text.NumberFormat
import kotlin.collections.ArrayList

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class DocumentAdapter: TypeAdapter<DictionaryDocument>() {

    override fun read(reader: JsonReader): DictionaryDocument? {

        var doc: DictionaryDocument? = null
        var name: String
        var value: Any?
        var jToken: JsonToken

        reader.beginObject()

        while (reader.hasNext()) {

            name = reader.nextName()
            jToken = reader.peek() //figure out what type we're working with

            if (jToken == JsonToken.NULL) {
                reader.nextNull() //consume it
                continue
            }

            when (name) {

                //we assume here that Id will ALWAYS come first and we can init doc here
                Resource.Companion.Keys.idKey -> doc = DictionaryDocument(reader.nextString())
                ResourceBase.Companion.resourceIdKey -> doc?.resourceId = reader.nextString()
                Resource.Companion.Keys.etagKey -> doc?.etag = reader.nextString()
                Resource.Companion.Keys.selfLinkKey -> doc?.selfLink = reader.nextString()
                Resource.Companion.Keys.timestampKey -> doc?.timestamp = Timestamp(reader.nextLong() * 1000)
                Document.Companion.Keys.attachmentsLinkKey -> doc?.attachmentsLink = reader.nextString()

                else -> {
                    //custom/user-defined data
                    value = readValue(reader, jToken)
                    //add it to our data map
                    doc?.data?.set(name, value)
                }
            }
        }

        reader.endObject()

        return doc
    }

    private fun readValue(reader: JsonReader, jToken: JsonToken) : Any? {

        return when (jToken) {

            JsonToken.BEGIN_ARRAY -> readArray(reader)

            JsonToken.BEGIN_OBJECT -> readObject(reader)

            JsonToken.STRING -> {

                val string = reader.nextString()

                //if this string is exactly the length of a formatted date, let's try to parse it as one
                if (string.length == RoundtripDateConverter.formattedDateLength) {
                    try {
                        return RoundtripDateConverter.toDate(string)
                    } catch (e: Exception) {
                        //not a date after all, just happens to be the same length ¯\_(ツ)_/¯
                        // TODO: maybe check this with a regex or do something else
                    }
                }

                return string
            }

            JsonToken.BOOLEAN -> reader.nextBoolean()

            JsonToken.NUMBER -> {

                val string = reader.nextString()
                NumberFormat.getInstance().parse(string) //TBD: do we need to cache this NumberFormat?
            }

            JsonToken.NULL -> {
                //consume it
                reader.nextNull()
                null
            }

            JsonToken.NAME -> throw Exception("Malformed JSON?  Not expecting another name at this position")
            JsonToken.END_DOCUMENT -> throw Exception("Unexpected END_DOCUMENT JSON...")
            else -> throw Exception("Unexpected null token or other error")
        }
    }

    private fun readArray(reader: JsonReader) : ArrayList<Any?> {

        val array: ArrayList<Any?> = ArrayList()

        reader.beginArray()

        while (reader.hasNext()) {

            val jToken = reader.peek()

            array.add(readValue(reader, jToken))
        }

        reader.endArray()

        return array
    }

    private fun readObject(reader: JsonReader) : Map<String, Any?> {

        //impossible to know what type this should deserialize to... we'll just map values
        val map: MutableMap<String, Any?> = mutableMapOf()

        reader.beginObject()

        while (reader.hasNext()) {

//            reader.skipValue()

            val name = reader.nextName()
            val value = reader.nextString()

            map[name] = value
        }

        reader.endObject()

        return map
    }

    override fun write(out: JsonWriter, value: DictionaryDocument?) {

        out.beginObject()

        value?.let {

            for (key in Resource.Companion.Keys.list) {

                out.name(key)

                when (key) {

                    Resource.Companion.Keys.idKey -> out.value(it.id)
                    ResourceBase.Companion.resourceIdKey -> out.value(it.resourceId)
                    Resource.Companion.Keys.etagKey -> out.value(it.etag)
                    Resource.Companion.Keys.selfLinkKey -> out.value(it.selfLink)
                    Resource.Companion.Keys.timestampKey -> out.jsonValue(gson.toJson(it.timestamp))
                }
            }

            out.name(Document.Companion.Keys.attachmentsLinkKey).value(it.attachmentsLink)

            for (dataItem in it.data) {

                out.name(dataItem.key)

                when (dataItem.value) {

                    is String -> out.value(dataItem.value as String)
                    is Double, is Float -> out.value(dataItem.value as Double)
                    is Long -> out.value(dataItem.value as Long)
                    is Number -> out.value(dataItem.value as Number)
                    is Boolean -> out.value(dataItem.value as Boolean)
                    else -> {
                        //all other types - serialize them!
                        val jsonValue = gson.toJson(dataItem.value)
                        out.jsonValue(jsonValue)
                    }
                }
            }
        }

        out.endObject()
    }
}