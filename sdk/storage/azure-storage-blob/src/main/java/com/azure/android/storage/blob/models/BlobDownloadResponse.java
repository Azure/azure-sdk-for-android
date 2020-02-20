// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

import okhttp3.ResponseBody;

/**
 * This class contains the response information return from the server when downloading a blob.
 */
public final class BlobDownloadResponse extends ResponseBase<BlobDownloadHeaders, Void> {
    /**
     * Constructs a {@link BlobDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public BlobDownloadResponse(BlobDownloadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
