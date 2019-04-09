package com.azure.data.service

import com.azure.data.AzureData
import com.azure.data.model.*
import com.azure.data.model.service.DataError
import okhttp3.Request
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ListResponse<T : Resource>(
        // The request sent to the server.
        request: Request? = null,
        // The server's response to the request.
        response: okhttp3.Response? = null,
        //  The json data returned by the server (if applicable)
        jsonData: String? = null,
        // The result of response deserialization.
        result: Result<ResourceList<T>>,
        // The resourceLocation, filled out when there could be more results
        resourceLocation: ResourceLocation? = null,
        // The Type of the Resource
        resourceType: Type? = null,
        // Whether the response is from the local cache or not.
        fromCache: Boolean = false
) : Response<ResourceList<T>>(request, response, jsonData, result, resourceLocation, resourceType, fromCache) {

    constructor(
            // the error
            error: DataError,
            // The URL request sent to the server.
            request: Request? = null,
            // The server's response to the URL request.
            response: okhttp3.Response? = null,
            // The json data returned by the server.
            jsonData: String? = null,
            // Whether the response is from the local cache or not.
            fromCache: Boolean = false) : this(request, response, jsonData, Result<ResourceList<T>>(error), fromCache = fromCache)

    constructor(result: ResourceList<T>) : this(result = Result(result))

    /**
     * Returns `true` if there are more paged results available
     */
    val hasMoreResults: Boolean
        get() {
            return !metadata.continuation.isNullOrEmpty()
        }

    /**
     * Uses the continuation found in the ListResponse metadata to fetch another page of resources from the server
     */
    fun next(callback: (ListResponse<T>) -> Unit) =
            AzureData.documentClient.next(this, callback)

    /**
     * Uses the continuation found in the ListResponse metadata to fetch another page of resources from the server
     */
    suspend fun next() =
            AzureData.documentClient.next(this)

    /**
     * Recursively grab more pages from a ListResponse
     */
    fun getMorePages(pagesToGet: Int? = null, callback: (ListResponse<T>) -> Unit) {

        if (this.hasMoreResults && pagesToGet ?: 1 > 0 && this.resource != null) {

            this.next {

                // add the last list of items to the next list of items and keep paying it forward
                it.resource?.items = this.resource.items + (it.resource?.items ?: listOf())

                it.getMorePages(if (pagesToGet != null) pagesToGet - 1 else null, callback)
            }
        } else {

            callback(this) // we're done, return to the caller
        }
    }

    /**
     * Recursively grab ALL pages from a ListResponse
     */
    fun getAllPages(callback: (ListResponse<T>) -> Unit) {

        this.getMorePages(callback = callback)
    }
}