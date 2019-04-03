package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpMediaType(val value: String) {

    Json("application/json"),
    QueryJson("application/query+json"),
    AtomXml("application/atom+xml")
}