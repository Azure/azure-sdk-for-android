// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

/**
 * Contains all response data for the stageBlock operation.
 */
public final class BlockBlobsStageBlockResponse extends ResponseBase<BlockBlobStageBlockHeaders, Void> {
    /**
     * Creates an instance of BlockBlobsStageBlockResponse.
     *
     * @param request the request which resulted in this BlockBlobsStageBlockResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public BlockBlobsStageBlockResponse(okhttp3.Request request,
                                        int statusCode,
                                        okhttp3.Headers rawHeaders,
                                        Void value,
                                        BlockBlobStageBlockHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
