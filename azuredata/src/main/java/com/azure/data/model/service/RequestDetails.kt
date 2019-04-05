package com.azure.data.model.service

import com.azure.core.http.HttpMethod
import com.azure.data.model.ResourceLocation

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

data class RequestDetails(val resourceLocation: ResourceLocation) {

    lateinit var method: HttpMethod

    var headers: MutableMap<String, String>? = null

    var partitionKey: String? = null

    var maxPerPage: Int? = null

    var body: ByteArray? = null

//    var resourceType: Type? = null

    constructor(resourceLocation: ResourceLocation, partitionKey: String?) : this(resourceLocation) {

        this.partitionKey = partitionKey
    }

    fun addHeader(key: String, value: String) {

        headers?.let {

            it[key] = value
        } ?: run {

            headers = mutableMapOf(key to value)
        }
    }
}