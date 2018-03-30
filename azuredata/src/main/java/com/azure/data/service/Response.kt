package com.azure.data.service

import com.azure.data.model.DataError
import com.azure.data.model.Result
import okhttp3.Request

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

open class Response(
        // The request sent to the server.
        val request: Request? = null,
        // The server's response to the request.
        val response: okhttp3.Response? = null,
        //  The json data returned by the server (if applicable)
        val jsonData: String? = null,
        // The result of response deserialization.
        open val result: Result<*>
) {

    constructor(
            // the error
            error: DataError,
            // The URL request sent to the server.
            request: Request? = null,
            // The server's response to the URL request.
            response: okhttp3.Response? = null,
            // The json data returned by the server.
            jsonData: String? = null) : this(request, response, jsonData, Result<Unit>(error))

    /**
     * Returns the associated error value if the result if it is a failure, null otherwise.
     */
    val error: DataError? get() = result.error

    /**
     * Returns `true` if the result is a success, `false` otherwise.
     */
    val isSuccessful get() = error == null

    /**
     * Returns `true` if the result is an error, `false` otherwise.
     */
    val isErrored get() = error != null
}