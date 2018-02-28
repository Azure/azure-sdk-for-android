package com.azure.data.services

import com.azure.data.model.*
import okhttp3.Request
import okhttp3.Response

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

class ResourceListResponse<T: Resource>(
        // The URL request sent to the server.
        request: Request? = null,
        // The server's response to the URL request.
        response: Response? = null,
        // The json data returned by the server.
        jsonData: String? = null,
        // The result of response deserialization.
        override val result: Result<ResourceList<T>>) : ResourceResponse<ResourceList<T>>(request, response, jsonData, result) {

    constructor(
            // the error
            error: DataError,
            // The URL request sent to the server.
            request: Request? = null,
            // The server's response to the URL request.
            response: Response? = null,
            // The json data returned by the server.
            jsonData: String? = null) : this(request, response, jsonData, Result(error))
}