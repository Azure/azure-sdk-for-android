package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

sealed class ApiValues {

    enum class HttpMethod {
        Get,
        Head,
        Post,
        Put,
        Delete
    }

    // https://docs.microsoft.com/en-us/rest/api/documentdb/common-documentdb-rest-request-headers
    enum class HttpRequestHeader(val value: String) {
        Authorization("Authorization"),
        ContentType("Content-Type"),
        IfMatch("If-Match"),
        IfNoneMatch("If-None-Match"),
        IfModifiedSince("If-Modified-Since"),
        UserAgent("User-Agent"),
        XMSActivityId("x-ms-activity-id"),
        XMSConsistencyLevel("x-ms-consistency-level"),
        XMSContinuation("x-ms-continuation"),
        XMSDate("x-ms-date"),
        XMSMaxItemCount("x-ms-max-item-count"),
        XMSDocumentDBPartitionKey("x-ms-documentdb-partitionkey"),
        XMSDocumentDBIsQuery("x-ms-documentdb-isquery"),
        XMSSessionToken("x-ms-session-token"),
        XMSVersion("x-ms-version"),
        AIM("A-IM"),
        XMSDocumentDBPartitionKeyRangeId("x-ms-documentdb-partitionkeyrangeid"),
        AcceptEncoding("Accept-Encoding"),
        AcceptLanguage("Accept-Language"),
        Slug("Slug");

        fun isRequired() : Boolean = when (this) {
            Authorization -> true
            ContentType -> true
            XMSDate -> true
            XMSSessionToken -> true
            XMSVersion -> true
            else -> false
        }
    }

    enum class HttpRequestHeaderValue(val value: String) {

        // https://docs.microsoft.com/en-us/rest/api/documentdb/#supported-rest-api-versions
        ApiVersion("2017-02-22"),

        // Accept-Encoding HTTP Header; see https://tools.ietf.org/html/rfc7230#section-4.2.3
        AcceptEncoding("gzip;q=1.0, compress;q=0.5")
    }

    enum class MediaTypes(val value: String) {

        Json("application/json"),
        QueryJson("application/query+json")
    }


    // https://docs.microsoft.com/en-us/rest/api/documentdb/http-status-codes-for-documentdb
    enum class StatusCode(val code: Int) {

        Ok(200),
        Created(201),
        NoContent(204),
        NotModified(304), //not documented, but 304 can be returned when specifying IfNoneMatch header with etag value
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
}