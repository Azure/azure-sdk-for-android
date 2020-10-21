// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Contains all response data for the setHTTPHeaders operation.
 */
public final class BlobSetHttpHeadersResponse extends ResponseBase<BlobSetHttpHeadersHeaders, Void> {
    /**
     * Creates an instance of BlobsSetHTTPHeadersResponse.
     *
     * @param request the request which resulted in this BlobsSetHTTPHeadersResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public BlobSetHttpHeadersResponse(Request request, int statusCode, Headers rawHeaders, Void value, BlobSetHttpHeadersHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
