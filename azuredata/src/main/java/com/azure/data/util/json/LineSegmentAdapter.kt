package com.azure.data.util.json

import com.azure.data.model.spatial.LineSegmentObject
import com.azure.data.model.spatial.LineString
import com.azure.data.model.spatial.Polygon
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class LineSegmentAdapter: JsonSerializer<LineSegmentObject>, JsonDeserializer<LineSegmentObject> {

    override fun serialize(src: LineSegmentObject?, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {

        return src?.let {

            val json = JsonObject()
            json.addProperty("type", when (src) { is Polygon -> "Polygon" else -> "LineString" })

            val coords = JsonArray()
            it.coordinates.forEach {  coord ->

                val jsonArr = JsonArray()
                // longitude first in GeoJSON spec
                jsonArr.add(coord[0])
                jsonArr.add(coord[1])

                coords.add(jsonArr)
            }

            json.add("coordinates", coords)
            json

        } ?: JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LineSegmentObject? {

        if (json.isJsonObject) {

            val jObj = json.asJsonObject
            val type = jObj.get("type").asString

            val list = mutableListOf<DoubleArray>()
            val jArr = jObj.get("coordinates").asJsonArray

            jArr.forEach {

                val coord = it.asJsonArray
                list.add(doubleArrayOf(coord[0].asDouble, coord[1].asDouble))
            }

            return when (type) {

                "Polygon" -> {

                    val polygon = Polygon()
                    polygon.coordinates.addAll(list)
                    polygon
                }
                "LineString" -> {

                    val lineString = LineString()
                    lineString.coordinates.addAll(list)
                    lineString
                }
                else -> null
            }
        }

        return null
    }
}