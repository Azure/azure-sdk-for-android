package com.azure.core.http

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
class HttpHeaderValue {

    companion object {

        // Accept-Encoding HTTP Header; see https://tools.ietf.org/html/rfc7230#section-4.2.3
        const val AcceptEncoding = "gzip;q=1.0, compress;q=0.5"
    }
}