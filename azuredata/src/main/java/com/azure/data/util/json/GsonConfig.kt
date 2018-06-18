package com.azure.data.util.json

import android.util.Log
import com.azure.core.log.logLevel
import com.azure.core.util.DateTypeAdapter
import com.google.gson.*
import com.azure.data.model.*
import com.azure.data.service.ResourceWriteOperation
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
*/

val gson: Gson =
        GsonBuilder()
                .disableHtmlEscaping()
                .checkVerboseMode()
                .registerTypeAdapter(Date::class.java, DateTypeAdapter())
                .registerTypeAdapter(Timestamp::class.java, TimestampAdapter())
                .registerTypeAdapter(DictionaryDocument::class.java, DocumentAdapter())
                .registerTypeAdapter(ResourceWriteOperation::class.java, ResourceWriteOperationAdapter())
                .create()

fun GsonBuilder.checkVerboseMode() : GsonBuilder {

    if (logLevel <= Log.DEBUG) {
        this.setPrettyPrinting()
    }

    return this
}