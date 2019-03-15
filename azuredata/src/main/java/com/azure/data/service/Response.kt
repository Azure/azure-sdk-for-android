package com.azure.data.service

import com.azure.data.model.*
import okhttp3.Request
import java.lang.reflect.Type

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

typealias DataResponse = Response<String>

open class Response<T>(
        // The request sent to the server.
        val request: Request? = null,
        // The server's response to the request.
        val response: okhttp3.Response? = null,
        //  The json data returned by the server (if applicable)
        val jsonData: String? = null,
        // The result of response deserialization.
        val result: Result<T>,
        // The resourceLocation, filled out when there could be more results
        val resourceLocation: ResourceLocation? = null,
        // The Type of the Resource
        val resourceType: Type? = null,
        // Whether the response is from the local cache or not.
        val fromCache: Boolean = false
) {
    val metadata : ResponseMetadata by lazy {
        ResponseMetadata(response)
    }

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
            fromCache: Boolean = false) : this(request, response, jsonData, Result<T>(error), fromCache = fromCache)

    constructor(result: T) : this(result = Result(result))

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

    /**
     * Returns the associated value of the result if it is a success, null otherwise.
     */
    val resource: T? = result.resource
}

fun <T, U> Response<T>.map(transform: (T) -> U): Response<U> {
    return Response(request, response, jsonData, result.map(transform), resourceLocation, resourceType, fromCache)
}

fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> {
    resource?.let { return Result(transform(it)) }
    error?.let { return Result(it) }
    return Result(DataError(DocumentClientError.UnknownError))
}