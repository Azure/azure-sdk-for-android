// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.implementation;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpResponse;
import com.azure.core.logging.ClientLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * HTTP response which will buffer the response's body.
 */
public final class BufferedHttpResponse extends HttpResponse {
    private final ClientLogger logger = new ClientLogger(BufferedHttpResponse.class);

    private final HttpResponse innerHttpResponse;
    private final BufferedInputStream bufferedBody;

    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
        this.bufferedBody = new BufferedInputStream(this.innerHttpResponse.getBody());
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
    public InputStream getBody() {
        return bufferedBody;
    }

    @Override
    public byte[] getBodyAsByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = this.bufferedBody.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toByteArray();
        } catch (IOException ioe) {
            throw logger.logExceptionAsError(new RuntimeException(ioe));
        }
    }

    @Override
    public String getBodyAsString() {
        return Util.bomAwareToString(this.getBodyAsByteArray(),
            innerHttpResponse.getHeaderValue("Content-Type"));
    }

    @Override
    public String getBodyAsString(Charset charset) {
        return new String(this.getBodyAsByteArray(), charset);
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }
}
