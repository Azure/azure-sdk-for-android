package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

// https://docs.microsoft.com/en-us/rest/api/documentdb/http-status-codes-for-documentdb
enum class HttpStatusCode(val code: Int) {

    Ok(200),
    Created(201),
    Accepted(202),
    NoContent(204),
    NotModified(304),
    BadRequest(400),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404),
    RequestTimeout(408),
    Conflict(409),
    PreconditionFailure(412),
    EntityTooLarge(413),
    TooManyRequests(429),
    RetryWith(449),
    InternalServerError(500),
    ServiceUnavailable(503)
}