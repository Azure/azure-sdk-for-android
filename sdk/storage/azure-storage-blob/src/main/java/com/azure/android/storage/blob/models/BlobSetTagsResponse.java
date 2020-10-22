// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Contains all response data for the setTags operation.
 */
public final class BlobSetTagsResponse extends ResponseBase<BlobSetTagsHeaders, Void> {
    /**
     * Creates an instance of BlobsSetTagsResponse.
     *
     * @param request the request which resulted in this BlobsSetTagsResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public BlobSetTagsResponse(Request request, int statusCode, Headers rawHeaders, Void value, BlobSetTagsHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
