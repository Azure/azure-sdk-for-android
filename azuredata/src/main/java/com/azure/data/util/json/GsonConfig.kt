package com.azure.data.util.json

import android.util.Log
import com.azure.core.log.logLevel
import com.azure.core.util.DateTypeAdapter
import com.google.gson.*
import com.azure.data.model.*
import com.azure.data.model.spatial.LineString
import com.azure.data.model.spatial.Point
import com.azure.data.model.spatial.Polygon
import com.azure.data.service.ResourceWriteOperation
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
*/

internal val gsonBuilder = GsonBuilder()
        .disableHtmlEscaping()
        .checkVerboseMode()
        .registerTypeAdapter(Date::class.java, DateTypeAdapter())
        .registerTypeAdapter(Timestamp::class.java, TimestampAdapter())
        .registerTypeAdapter(Point::class.java, PointAdapter())
        .registerTypeAdapter(Polygon::class.java, LineSegmentAdapter())
        .registerTypeAdapter(LineString::class.java, LineSegmentAdapter())
        .registerTypeAdapter(ResourceWriteOperation::class.java, ResourceWriteOperationAdapter())!!

lateinit var gson: Gson

fun GsonBuilder.checkVerboseMode() : GsonBuilder {

    if (logLevel <= Log.DEBUG) {
        this.setPrettyPrinting()
    }

    return this
}