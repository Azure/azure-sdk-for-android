package com.azure.data.model.service

import com.azure.core.http.HttpMethod
import com.azure.data.model.Resource
import com.azure.data.model.ResourceLocation
import java.lang.reflect.Type

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

    var resourceType: Type? = null

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

    companion object {

        fun <T : Resource> fromResource(resource: T, partitionKey: String? = null): RequestDetails {

            val details = RequestDetails(ResourceLocation.Resource(resource))
            details.resourceType = resource::class.java
            details.partitionKey = partitionKey

            return details
        }
    }
}