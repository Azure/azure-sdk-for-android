// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import okhttp3.ResponseBody;

/**
 * Rest API response with a streaming content.
 */
public class StreamResponse extends Response<Reader> implements Closeable {
    private final retrofit2.Response<ResponseBody> innerResponse;

    /**
     * Creates StreamResponse.
     *
     * @param innerResponse The Retrofit response backing this stream response.
     */
    public StreamResponse(@NonNull retrofit2.Response<ResponseBody> innerResponse) {
        super(innerResponse.raw().request(),
            innerResponse.code(),
            innerResponse.headers(),
            innerResponse.body().charStream());
        this.innerResponse = innerResponse;
    }

    /**
     * This method loads entire response body into memory. If the response body is very large this may trigger an
     * [OutOfMemoryError]. Prefer to stream the response body using {@link this#getValue()} if this is a possibility
     * for your response.
     *
     * @return The response content as a byte array.
     * @throws IOException In case there is a problem reading the response body.
     */
    public byte[] getBytes() throws IOException {
        return this.innerResponse.body().bytes();
    }

    @Override
    public void close() throws IOException {
        this.innerResponse.body().close();
    }
}
