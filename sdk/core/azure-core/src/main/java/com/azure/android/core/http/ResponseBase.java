// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

/**
 * Rest API response with a strongly-typed header and content.
 *
 * @param <H> The type of the response headers.
 * @param <T> The type of the response value, available by using {@link Response#getValue()}.
 */
public class ResponseBase<H, T> extends Response<T> {
    private final H deserializedHeaders;

    /**
     * Creates REST response.
     *
     * @param request             The request that resulted in this response.
     * @param statusCode          The HTTP response status code.
     * @param headers             The HTTP response header.
     * @param value               The HTTP response content as a strongly typed instance.
     * @param deserializedHeaders The HTTP response headers as a strongly typed instance.
     */
    public ResponseBase(okhttp3.Request request,
                        int statusCode,
                        okhttp3.Headers headers,
                        T value,
                        H deserializedHeaders) {
        super(request, statusCode, headers, value);

        this.deserializedHeaders = deserializedHeaders;
    }

    /**
     * Get the headers from the HTTP response, transformed into the header type, {@code H}.
     *
     * @return An instance of header type {@code H}, deserialized from the HTTP response headers.
     */
    public H getDeserializedHeaders() {
        return deserializedHeaders;
    }
}
