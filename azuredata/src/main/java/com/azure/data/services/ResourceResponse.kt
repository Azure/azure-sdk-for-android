package com.azure.data.services

import com.azure.data.model.DataError
import com.azure.data.model.ResourceBase
import com.azure.data.model.Result
import okhttp3.Request

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class ResourceResponse<T : ResourceBase>(
        // The URL request sent to the server.
        request: Request? = null,
        // The server's response to the URL request.
        response: okhttp3.Response? = null,
        // The data returned by the server.
        jsonData: String? = null,
        // The result of response deserialization.
        override val result: Result<T>) : Response(request, response, jsonData, result) {

    constructor(
            // the error
            error: DataError,
            // The URL request sent to the server.
            request: Request? = null,
            // The server's response to the URL request.
            response: okhttp3.Response? = null,
            // The json data returned by the server.
            jsonData: String? = null) : this(request, response, jsonData, Result(error))

    /**
     * Returns the associated value of the result if it is a success, null otherwise.
     */
    val resource: T? get() = result.resource
}