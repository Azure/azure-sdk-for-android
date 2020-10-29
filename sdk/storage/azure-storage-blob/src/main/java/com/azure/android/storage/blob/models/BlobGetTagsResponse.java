// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Contains all response data for the getTags operation.
 */
public final class BlobGetTagsResponse extends ResponseBase<BlobGetTagsHeaders, BlobTags> {

    /**
     * Creates an instance of {@link BlobGetTagsResponse}.
     *
     * @param request             The request which resulted in this BlobGetTagsResponse.
     * @param statusCode          The status code of the HTTP response.
     * @param rawHeaders          The raw headers of the HTTP response.
     * @param value               The deserialized value of the HTTP response.
     * @param deserializedHeaders The deserialized headers of the HTTP response.
     */
    public BlobGetTagsResponse(Request request,
                               int statusCode,
                               Headers rawHeaders,
                               BlobTags value,
                               BlobGetTagsHeaders deserializedHeaders) {
        super(request, statusCode, rawHeaders, value, deserializedHeaders);
    }
}
