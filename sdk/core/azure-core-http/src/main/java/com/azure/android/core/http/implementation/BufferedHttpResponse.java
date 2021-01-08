// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.implementation;

import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpResponse;
import com.azure.core.logging.ClientLogger;

import java.io.ByteArrayInputStream;
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
    private byte[] bufferedContent;
    private volatile boolean closed;


    /**
     * Creates a buffered HTTP response.
     *
     * @param innerHttpResponse The HTTP response to buffer.
     */
    public BufferedHttpResponse(HttpResponse innerHttpResponse) {
        super(innerHttpResponse.getRequest());
        this.innerHttpResponse = innerHttpResponse;
    }

    @Override
    public int getStatusCode() {
        return this.innerHttpResponse.getStatusCode();
    }

    @Override
    public String getHeaderValue(String name) {
        return this.innerHttpResponse.getHeaderValue(name);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.innerHttpResponse.getHeaders();
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.getBodyAsByteArray());
    }

    @Override
    public synchronized byte[] getBodyAsByteArray() {
        if (this.bufferedContent == null) {
            InputStream innerStream = this.innerHttpResponse.getBody();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = innerStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                this.bufferedContent = outStream.toByteArray();
            } catch (IOException ioe) {
                throw logger.logExceptionAsError(new RuntimeException(ioe));
            }
        }
        return this.bufferedContent;
    }

    @Override
    public String getBodyAsString() {
        return Util.bomAwareToString(this.getBodyAsByteArray(),
            this.innerHttpResponse.getHeaderValue("Content-Type"));
    }

    @Override
    public String getBodyAsString(Charset charset) {
        return new String(this.getBodyAsByteArray(), charset);
    }

    @Override
    public BufferedHttpResponse buffer() {
        return this;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.innerHttpResponse.close();
            this.closed = true;
            super.close();
        }
    }
}
