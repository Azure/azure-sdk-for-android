package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpMediaType(val value: String) {

    Any("*/*"),
    Json("application/json"),
    QueryJson("application/query+json"),
    AtomXml("application/atom+xml"),
    Jpeg("image/jpeg"),
    Png("image/png"),
    Javascript("application/x-javascript"),
    OctetStream("application/octet-stream"),
    SQL("application/sql"),
    TextHtml("text/html"),
    TextPlain("text/plain"),
    Xml("application/xml")
}