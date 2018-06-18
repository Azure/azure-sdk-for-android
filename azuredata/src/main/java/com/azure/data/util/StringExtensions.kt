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

fun String.lastPathComponent(): String {
    val components = split("/")
    val count = components.count()

    return if (count > 1) components[count - 1] else this
}

fun String.lastPathComponentRemoved(): String {
    val components = split("/")
    return components.dropLast(1).joinToString("/")
}

fun String.ancestorPath(): String {
    val components = split("/")
    return components.dropLast(2).joinToString("/")
}