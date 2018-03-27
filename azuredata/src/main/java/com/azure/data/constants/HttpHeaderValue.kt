package com.azure.data.constants

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

enum class HttpHeaderValue(val value: String) {

    // https://docs.microsoft.com/en-us/rest/api/documentdb/#supported-rest-api-versions
    ApiVersion("2017-02-22"),

    // Accept-Encoding HTTP Header; see https://tools.ietf.org/html/rfc7230#section-4.2.3
    AcceptEncoding("gzip;q=1.0, compress;q=0.5")
}