// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;

import com.azure.android.core.http.ResponseBase;

/**
 * Contains all response data for the listBlobFlatSegment operation.
 */
public final class ContainersListBlobFlatSegmentResponse
        extends ResponseBase<ContainerListBlobFlatSegmentHeaders, ListBlobsFlatSegmentResponse> {

    /**
     * Creates an instance of ContainersListBlobFlatSegmentResponse.
     *
     * @param request the request which resulted in this ContainersListBlobFlatSegmentResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public ContainersListBlobFlatSegmentResponse(okhttp3.Request request,
                                                 int statusCode,
                                                 okhttp3.Headers rawHeaders,
                                                 ListBlobsFlatSegmentResponse value,
                                                 ContainerListBlobFlatSegmentHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /**
     * @return the decoded response body.
     */
    @Override
    public ListBlobsFlatSegmentResponse getValue() {
        return super.getValue();
    }
}
