// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, ResponseBody> {
    /**
     * Constructs a {@link BlobDownloadAsyncResponse}.
     *
     * @param request             Request sent to the service.
     * @param statusCode          Response status code returned by the service.
     * @param headers             Raw headers returned in the response.
     * @param value               Downloaded data being returned by the service.
     * @param deserializedHeaders Headers deserialized into an object.
     */
    public BlobDownloadAsyncResponse(Request request, int statusCode, Headers headers, ResponseBody value,
                                     BlobDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, value, deserializedHeaders);
    }
}
