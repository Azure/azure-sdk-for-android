package com.azure.data.integration.common

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

internal class SampleTypeAdapter: JsonSerializer<CustomDocument>, JsonDeserializer<CustomDocument> {

    override fun serialize(src: CustomDocument?, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {

        return JsonPrimitive("test")
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CustomDocument? {

        return CustomDocument("test")
    }
}