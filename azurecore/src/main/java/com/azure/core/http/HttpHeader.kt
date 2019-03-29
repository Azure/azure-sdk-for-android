package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpHeader(val value: String) {

    Accept("Accept"),
    AcceptCharset("Accept-Charset"),
    AcceptEncoding("Accept-Encoding"),
    AcceptLanguage("Accept-Language"),
    AcceptRanges("Accept-Ranges"),
    Age("Age"),
    Allow("Allow"),
    /**
     * The authorization token for the request. For more information on generating a valid authorization token, see Access Control on Cosmos DB Resources.
     */
    Authorization("Authorization"),
    CacheControl("Cache-Control"),
    Connection("Connection"),
    ContentEncoding("Content-Encoding"),
    ContentLanguage("Content-Language"),
    ContentLength("Content-Length"),
    ContentLocation("Content-Location"),
    ContentRange("Content-Range"),
    /**
     * Requests:
     * For POST on query operations, it must be application/query+json.
     * For attachments, must be set to the Mime type of the attachment.
     * For all other tasks, must be application/json.
     * Responses:
     * The Content-Type is application/json. The SQL API always returns the response body in standard JSON format.
     */
    ContentType("Content-Type"),
    /**
     * The date time of the response operation. This date time format conforms to the RFC 1123 date time format expressed in Coordinated Universal Time.
     */
    Date("Date"),
    /**
     * The etag header shows the resource etag for the resource retrieved. The etag has the same value as the _etag property in the response body.
     */
    ETag("ETag"),
    Expect("Expect"),
    Expires("Expires"),
    From("From"),
    Host("Host"),
    /**
     * Used to make operation conditional for optimistic concurrency. The value should be the etag value of the resource.
     */
    IfMatch("If-Match"),
    /**
     * Returns etag of resource modified after specified date in RFC 1123 format. Ignored when If-None-Match is specified.
     */
    IfModifiedSince("If-Modified-Since"),
    /**
     * The value should be the etag of the resource. Makes operation conditional, that is, the response includes a body only the value in the database is different from the specified value in the header.
     */
    IfNoneMatch("If-None-Match"),
    IfUnmodifiedSince("If-Unmodified-Since"),
    LastModified("Last-Modified"),
    Location("Location"),
    Pragma("Pragma"),
    Range("Range"),
    Referer("Referer"),
    Server("Server"),
    Slug("Slug"),
    Trailer("Trailer"),
    TransferEncoding("Transfer-Encoding"),
    /**
     * A string that specifies the client user agent performing the request. The recommended format is {user agent name}/{version}. For example, the official SQL API .NET SDK sets the User-Agent string to Microsoft.Document.Client/1.0.0.0. A custom user-agent could be something like ContosoMarketingApp/1.0.0.
     */
    UserAgent("User-Agent"),
    Vary("Vary"),
    Via("Via"),
    Warning("Warning"),
    WWWAuthenticate("WWW-Authenticate")
}