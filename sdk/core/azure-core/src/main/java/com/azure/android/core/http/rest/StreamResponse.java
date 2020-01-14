package com.azure.android.core.http.rest;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import okhttp3.ResponseBody;

/**
 * REST response with a streaming content.
 */
public class StreamResponse extends Response<Reader> implements Closeable {
    private final retrofit2.Response<ResponseBody> inner;

    /**
     * Creates StreamResponse
     *
     * @param inner the getRetrofit response backing this stream response
     */
    public StreamResponse(retrofit2.Response<ResponseBody> inner) {
        super(inner.raw().request(), inner.code(), inner.headers(), inner.body().charStream());
        this.inner = inner;
    }

    /**
     * Returns the response as a byte array.
     *
     * This method loads entire response body into memory. If the response body is very
     * large this may trigger an [OutOfMemoryError]. Prefer to stream the response body
     * using {@link this#getValue()} if this is a possibility for your response.
     *
     * @return the response content as byte array
     * @throws IOException
     */
    public byte[] getBytes() throws IOException {
        return this.inner.body().bytes();
    }

    @Override
    public void close() throws IOException {
        this.inner.body().close();
    }
}
