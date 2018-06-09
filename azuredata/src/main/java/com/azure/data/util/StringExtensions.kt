package com.azure.data.util

import com.azure.data.model.DataError
import com.azure.data.util.json.gson

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

fun String.toError(): DataError =
        gson.fromJson(this, DataError::class.java)

fun String.extractId(resourceType: String): String? {
    val components = split("/")
    val index = components.indexOf(resourceType)

    if (index >= 0 && index + 1 < components.count()) {
        return components[index + 1]
    }

    return null
}