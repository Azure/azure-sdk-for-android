package com.azure.data.util.json

import com.azure.core.RoundtripDateConverter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class DateTypeAdapter : TypeAdapter<Date>() {

    override fun read(`in`: JsonReader?): Date? {

        val dateString = `in`?.nextString()

        return RoundtripDateConverter.toDate(dateString)
    }

    override fun write(out: JsonWriter, value: Date?) {

        out.value(RoundtripDateConverter.toString(value))
    }
}