// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * HTTP response which will buffer the response's body when/if it is read.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final HttpResponse innerHttpResponse;
    private final Flux<ByteBuffer> cachedBody;

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
        this.cachedBody = innerHttpResponse.getBody()
            .collect(ByteArrayOutputStream::new, BufferedHttpResponse::accept)
            .map(ByteArrayOutputStream::toByteArray)
            .map(ByteBuffer::wrap)
            .flux()
            .cache();
    }

    private static void accept(ByteArrayOutputStream byteOutputStream, ByteBuffer byteBuffer) {
        try {
            byteOutputStream.write(byteBufferToArray(byteBuffer));
        } catch (IOException e) {
            throw new RuntimeException("Error occurred writing ByteBuffer to ByteArrayOutputStream.", e);
        }
    }

    private static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.remaining();
        byte[] byteArray = new byte[length];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    @Override
    public int getStatusCode() {
        return innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return innerHttpResponse.getHeaders();
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return cachedBody;
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return cachedBody.next().map(ByteBuffer::array);
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(bytes ->
            Util.bomAwareToString(bytes, innerHttpResponse.getHeaderValue("Content-Type")));
    }

    @Override
    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray()
                .map(bytes -> bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
