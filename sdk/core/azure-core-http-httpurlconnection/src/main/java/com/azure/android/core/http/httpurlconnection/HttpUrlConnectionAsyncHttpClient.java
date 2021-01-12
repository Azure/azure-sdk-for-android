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
import com.azure.android.core.http.implementation.Util;
import com.azure.core.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
            this.httpCallDispatcher.enqueue(new HttpCallDispatcher.HttpCallFunction() {
                @Override
                public void apply(HttpRequest request, HttpCallback httpCallback) {
                    sendIntern(request, httpCallback);
                }
            }, httpRequest, httpCallback);
        }
    }

    private void sendIntern(HttpRequest httpRequest, HttpCallback httpCallback) {
        if (httpRequest.getCancellationToken().isCancellationRequested()) {
            httpCallback.onError(new IOException("Canceled."));
            return;
        }

        final HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) httpRequest.getUrl().openConnection();
        } catch (IOException ioe) {
            httpCallback.onError(ioe);
            return;
        }

        connection.setDoInput(true);

        Throwable error = null;
        HttpResponse httpResponse = null;
        boolean hasResponseContent = false;
        try {
            // Request: headers
            for (HttpHeader header : httpRequest.getHeaders()) {
                connection.addRequestProperty(header.getName(), header.getValue());
            }

            // Request: method and content.
            switch (httpRequest.getHttpMethod()) {
                case GET:
                case HEAD:
                case OPTIONS:
                case TRACE:
                case DELETE:
                    connection.setRequestMethod(httpRequest.getHttpMethod().toString());
                    break;
                case PUT:
                case POST:
                case PATCH:
                    connection.setRequestMethod(httpRequest.getHttpMethod().toString());
                    final byte[] requestContent = httpRequest.getBody();
                    if (requestContent != null) {
                        connection.setDoOutput(true);
                        final OutputStream requestContentStream = connection.getOutputStream();
                        try {
                            requestContentStream.write(requestContent);
                        } finally {
                            requestContentStream.close();
                        }
                    }
                    break;
                default:
                    throw logger.logExceptionAsError(new IllegalStateException("Unknown HTTP Method:"
                        + httpRequest.getHttpMethod()));
            }

            // Response: StatusCode
            final int statusCode = connection.getResponseCode();
            if (statusCode == -1) {
                final IOException ioException = new IOException("Retrieval of HTTP response code failed. "
                    + "HttpUrlConnection::getResponseCode() returned -1");
                throw logger.logExceptionAsError(new RuntimeException(ioException));
            }

            // Response: headers
            final Map<String, List<String>> connHeaderMap = connection.getHeaderFields();
            final HttpHeaders headers = new HttpHeaders();
            for (Map.Entry<String, List<String>> entry : connHeaderMap.entrySet()) {
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

            // Response: Content
            hasResponseContent = statusCode != HttpURLConnection.HTTP_NO_CONTENT
                && statusCode != HttpURLConnection.HTTP_NOT_MODIFIED
                && statusCode >= HttpURLConnection.HTTP_OK
                && httpRequest.getHttpMethod() != HttpMethod.HEAD;

            final InputStream responseContentStream = hasResponseContent
                ? new ResponseContentStream(connection)
                : new ByteArrayInputStream(new byte[0]);

            httpResponse = new UrlConnectionResponse(logger,
                httpRequest,
                statusCode,
                headers,
                responseContentStream);
        } catch (Throwable e) {
            error = e;
        } finally {
            if (error != null || !hasResponseContent) {
                connection.disconnect();
            }
        }

        if (error != null) {
            httpCallback.onError(error);
            return;
        } else {
            httpCallback.onSuccess(httpResponse);
        }
    }

    private static class ResponseContentStream extends FilterInputStream {
        private final HttpURLConnection innerConnection;

        ResponseContentStream(HttpURLConnection connection) {
            super(innerInputStream(connection));
            this.innerConnection = connection;
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.innerConnection.disconnect();
        }

        private static InputStream innerInputStream(HttpURLConnection connection) {
            try {
                // try reading from input-stream..
                return connection.getInputStream();
            } catch (IOException ioe) {
                // input-stream read can throw IOE for responses with error HTTP code (e.g. 400), try error-stream..
                return connection.getErrorStream();
            }
        }
    }

    private static class UrlConnectionResponse extends HttpResponse {
        private final ClientLogger logger;
        private final int statusCode;
        private final HttpHeaders headers;
        private final InputStream contentStream;

        UrlConnectionResponse(ClientLogger logger,
                              HttpRequest request,
                              int statusCode,
                              HttpHeaders headers,
                              InputStream contentStream) {
            super(request);
            this.logger = logger;
            this.statusCode = statusCode;
            this.headers = headers;
            this.contentStream = contentStream;
        }

        @Override
        public int getStatusCode() {
            return this.statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return this.headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        @Override
        public InputStream getBody() {
            return this.contentStream;
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
    }
}
