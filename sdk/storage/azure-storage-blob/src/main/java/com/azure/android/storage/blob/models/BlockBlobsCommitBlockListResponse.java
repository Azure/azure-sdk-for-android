// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

/**
 * Contains all response data for the commitBlockList operation.
 */
public final class BlockBlobsCommitBlockListResponse extends ResponseBase<BlockBlobCommitBlockListHeaders, Void> {

    /**
     * Creates an instance of BlockBlobsCommitBlockListResponse.
     *
     * @param request the request which resulted in this BlockBlobsCommitBlockListResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public BlockBlobsCommitBlockListResponse(okhttp3.Request request,
                                             int statusCode,
                                             okhttp3.Headers rawHeaders,
                                             Void value,
                                             BlockBlobCommitBlockListHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}

