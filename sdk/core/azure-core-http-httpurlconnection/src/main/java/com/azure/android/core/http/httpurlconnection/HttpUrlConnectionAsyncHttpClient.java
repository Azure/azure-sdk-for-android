// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.httpurlconnection;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpHeader;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpResponse;
import com.azure.core.http.implementation.Util;
import com.azure.core.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class HttpUrlConnectionAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(HttpUrlConnectionAsyncHttpClient.class);

    private final HttpCallDispatcher httpCallDispatcher;

    HttpUrlConnectionAsyncHttpClient(HttpCallDispatcher httpCallDispatcher) {
        this.httpCallDispatcher = httpCallDispatcher;
    }

    @Override
    public HttpCallDispatcher getHttpCallDispatcher() {
        return this.httpCallDispatcher;
    }

    @Override
    public void send(HttpRequest httpRequest, HttpCallback httpCallback) {
        boolean isFromPipeline = true; // TODO: read this flag from httpRequest.context.

        if (isFromPipeline) {
            this.sendIntern(httpRequest, httpCallback);
        } else {
            this.httpCallDispatcher.enqueue((httpRequest1, httpCallback1) -> {
                sendIntern(httpRequest1, httpCallback1);
            }, httpRequest, httpCallback);
        }
    }

    private void sendIntern(HttpRequest httpRequest, HttpCallback httpCallback) {
        final HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) httpRequest.getUrl().openConnection();
        } catch (IOException ioe) {
            httpCallback.onError(ioe);
            return;
        }

        try {
            connection.setRequestMethod(httpRequest.getHttpMethod().toString());
        } catch (ProtocolException pe) {
            httpCallback.onError(pe);
            return;
        }

        connection.setDoOutput(true);
        for (HttpHeader header : httpRequest.getHeaders()) {
            connection.addRequestProperty(header.getName(), header.getValue());
        }

        if (httpRequest.getHttpMethod() != HttpMethod.GET || httpRequest.getHttpMethod() == HttpMethod.HEAD) {
            try {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(httpRequest.getBody());
                outputStream.flush();
                outputStream.close();
            } catch (IOException ioe) {
                httpCallback.onError(ioe);
                return;
            }
        }

        final int statusCode;
        try {
            statusCode = connection.getResponseCode();
        } catch (IOException ioe) {
            httpCallback.onError(ioe);
            return;
        }

        final Map<String, List<String>> map = connection.getHeaderFields();
        final HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            final String headerName = entry.getKey();
            final String headerValue;
            Iterator<String> hdrValueItr = entry.getValue().iterator();
            if (hdrValueItr.hasNext()) {
                headerValue = hdrValueItr.next();
            } else {
                headerValue = null;
            }
            headers.put(headerName, headerValue);
        }

        httpCallback.onSuccess(new HttpResponse(httpRequest) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                return headers.getValue(name);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }

            @Override
            public InputStream getBody() {
                try {
                    return connection.getInputStream();
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(new RuntimeException(ioe));
                }
            }

            @Override
            public byte[] getBodyAsByteArray() {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                InputStream is = this.getBody();
                try {
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(new RuntimeException(ioe));
                } finally {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        throw logger.logExceptionAsError(new RuntimeException(ioe));
                    }
                }
                return buffer.toByteArray();
            }

            @Override
            public String getBodyAsString() {
                return Util.bomAwareToString(this.getBodyAsByteArray(),
                    headers.getValue("Content-Type"));
            }

            @Override
            public String getBodyAsString(Charset charset) {
                return new String(this.getBodyAsByteArray(), charset);
            }

            @Override
            public void close() {
                try {
                    getBody().close();
                } catch (IOException ioe) {
                    throw logger.logExceptionAsError(new RuntimeException(ioe));
                }
            }
        });
    }
}
