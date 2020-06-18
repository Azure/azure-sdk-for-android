// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Contains all response data for the delete operation.
 */
public final class BlobDeleteResponse extends ResponseBase<BlobDeleteHeaders, Void> {
    /**
     * Creates an instance of {@link BlobDeleteResponse}.
     *
     * @param request             The request which resulted in this BlobsDeleteResponse.
     * @param statusCode          The status code of the HTTP response.
     * @param rawHeaders          The raw headers of the HTTP response.
     * @param value               The deserialized value of the HTTP response.
     * @param deserializedHeaders The deserialized headers of the HTTP response.
     */
    public BlobDeleteResponse(Request request,
                              int statusCode,
                              Headers rawHeaders,
                              Void value,
                              BlobDeleteHeaders deserializedHeaders) {
        super(request, statusCode, rawHeaders, value, deserializedHeaders);
    }
}
