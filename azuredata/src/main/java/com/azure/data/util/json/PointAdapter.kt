package com.azure.data.util.json

import com.google.gson.*
import com.azure.data.model.spatial.Point
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class PointAdapter: JsonSerializer<Point>, JsonDeserializer<Point> {

    override fun serialize(src: Point?, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {

        return src?.let {

            val json = JsonObject()
            json.addProperty("type", "Point")

            val coords = JsonArray()

            // longitude first in GeoJSON spec
            coords.add(it.longitude)
            coords.add(it.latitude)

            json.add("coordinates", coords)
            json

        } ?: JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Point? {

        if (json.isJsonObject) {

            val jObj = json.asJsonObject
            val type = jObj.get("type").asString

            if (type == "Point") {

                val jArr = jObj.get("coordinates").asJsonArray

                if (jArr.size() == 2) {

                    // longitude first in GeoJSON spec
                    return Point(jArr[0].asDouble, jArr[1].asDouble)
                }
            }
        }

        return null
    }
}