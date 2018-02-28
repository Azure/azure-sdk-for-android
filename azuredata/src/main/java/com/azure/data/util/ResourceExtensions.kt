package com.azure.data.util

import com.google.gson.reflect.TypeToken
import com.azure.data.model.Resource
import com.azure.data.util.json.gson

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceExtensions {

    fun Resource.toJson() : String =
            gson.toJson(this)

    fun<T: Resource> Resource.fromJson(json: String) : T =
            gson.fromJson(json, object : TypeToken<T>() {}.type)
}