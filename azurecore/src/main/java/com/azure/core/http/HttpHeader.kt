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
    Authorization("Authorization"),
    CacheControl("Cache-Control"),
    Connection("Connection"),
    ContentEncoding("Content-Encoding"),
    ContentLanguage("Content-Language"),
    ContentLength("Content-Length"),
    ContentLocation("Content-Location"),
    ContentRange("Content-Range"),
    ContentType("Content-Type"),
    Date("Date"),
    ETag("ETag"),
    Expect("Expect"),
    Expires("Expires"),
    From("From"),
    Host("Host"),
    IfMatch("If-Match"),
    IfModifiedSince("If-Modified-Since"),
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
    UserAgent("User-Agent"),
    Vary("Vary"),
    Via("Via"),
    Warning("Warning"),
    WWWAuthenticate("WWW-Authenticate")
}