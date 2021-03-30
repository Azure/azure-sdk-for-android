// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.android.core.rest;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpRequest;

import java.io.Closeable;
import java.io.InputStream;

/**
 * REST response with a streaming content.
 */
public final class StreamResponse extends SimpleResponse<InputStream> implements Closeable {
    /**
     * Creates a {@link StreamResponse}.
     *
     * @param request The request which resulted in this response.
     * @param statusCode The status code of the HTTP response.
     * @param headers The headers of the HTTP response.
     * @param value The content of the HTTP response.
     */
    public StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, InputStream value) {
        super(request, statusCode, headers, value);
    }

    /**
     * Disposes the connection associated with this {@link StreamResponse}.
     */
    @Override
    public void close() {
        // TODO: anuchan implement close
    }
}
