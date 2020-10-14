// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob.models;


import com.azure.android.core.http.ResponseBase;
import com.azure.android.storage.blob.models.ContainerGetPropertiesHeaders;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Contains all response data for the container getProperties operation.
 */
public final class ContainerGetPropertiesResponse extends ResponseBase<ContainerGetPropertiesHeaders, Void> {
    /**
     * Creates an instance of ContainerGetPropertiesResponse.
     *
     * @param request the request which resulted in this ContainersGetPropertiesResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public ContainerGetPropertiesResponse(Request request, int statusCode, Headers rawHeaders, Void value,
                                          ContainerGetPropertiesHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
