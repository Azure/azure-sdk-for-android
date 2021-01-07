// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.test.http;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * An HttpClient instance that returns "code:200 content: no-op-http-client" response.
 */
public class NoOpHttpClient implements HttpClient {

    @Override
    public HttpCallDispatcher getHttpCallDispatcher() {
        return new HttpCallDispatcher();
    }

    @Override
    public void send(HttpRequest httpRequest, HttpCallback httpCallback) {
        httpCallback.onSuccess(new DefaultHttpResponse(httpRequest));
    }

    private static final class DefaultHttpResponse extends HttpResponse {
        private final HttpHeaders httpHeaders = new HttpHeaders();
        private final byte[] content = new String("no-op-http-client").getBytes();

        protected DefaultHttpResponse(HttpRequest httpRequest) {
            super(httpRequest);
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(this.content);
        }

        @Override
        public byte[] getBodyAsByteArray() {
            byte[] copy = new byte[this.content.length];
            System.arraycopy(this.content, 0, copy, 0, this.content.length);
            return copy;
        }

        @Override
        public String getBodyAsString() {
            return new String(this.content);
        }

        @Override
        public String getBodyAsString(Charset charset) {
            return getBodyAsString();
        }
    }
}
