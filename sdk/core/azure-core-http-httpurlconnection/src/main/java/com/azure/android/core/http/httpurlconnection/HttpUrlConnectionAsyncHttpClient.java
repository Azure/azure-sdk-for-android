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
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HttpUrlConnectionAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(HttpUrlConnectionAsyncHttpClient.class);

    private static final Pattern CHARSET_PATTERN
        = Pattern.compile("charset=([\\S]+)\\b", Pattern.CASE_INSENSITIVE);

    private final HttpCallDispatcher httpCallDispatcher;

    HttpUrlConnectionAsyncHttpClient(HttpCallDispatcher httpCallDispatcher) {
        this.httpCallDispatcher = httpCallDispatcher;
    }

    @Override
    public HttpCallDispatcher getHttpCallDispatcher() {
        return this.httpCallDispatcher;
    }

    @Override
    public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
        if (httpRequest.getTags().containsKey("prefer-running-http-in-calling-thread")) {
            this.sendIntern(httpRequest, cancellationToken, httpCallback);
        } else {
            this.httpCallDispatcher.enqueue(new HttpCallDispatcher.HttpCallFunction() {
                @Override
                public void apply(HttpRequest request, HttpCallback httpCallback) {
                    sendIntern(request, cancellationToken, httpCallback);
                }
            }, httpRequest, cancellationToken, httpCallback);
        }
    }

    private void sendIntern(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
        if (cancellationToken.isCancellationRequested()) {
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
                    connection.setRequestMethod(httpRequest.getHttpMethod().toString());
                    break;
                case PUT:
                case POST:
                case PATCH:
                case DELETE:
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
            return bomAwareToString(this.getBodyAsByteArray(),
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

        /**
         * Attempts to convert a byte stream into the properly encoded String.
         * <p>
         * The method will attempt to find the encoding for the String in this order.
         * <ol>
         *     <li>Find the byte order mark in the byte array.</li>
         *     <li>Find the {@code charset} in the {@code Content-Type} header.</li>
         *     <li>Default to {@code UTF-8}.</li>
         * </ol>
         *
         * @param bytes Byte array.
         * @param contentType {@code Content-Type} header value.
         * @return A string representation of the byte array encoded to the found encoding.
         */
        private String bomAwareToString(byte[] bytes, String contentType) {
            if (bytes == null) {
                return null;
            }

            if (bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF) {
                return new String(bytes, 3, bytes.length - 3, Charset.forName("UTF-8"));
            } else if (bytes.length >= 4
                && bytes[0] == (byte) 0x00
                && bytes[1] == (byte) 0x00
                && bytes[2] == (byte) 0xFE
                && bytes[3] == (byte) 0xFF) {
                return new String(bytes, 4, bytes.length - 4, Charset.forName("UTF-32BE"));
            } else if (bytes.length >= 4
                && bytes[0] == (byte) 0xFF
                && bytes[1] == (byte) 0xFE
                && bytes[2] == (byte) 0x00
                && bytes[3] == (byte) 0x00) {
                return new String(bytes, 4, bytes.length - 4, Charset.forName("UTF-32LE"));
            } else if (bytes.length >= 2
                && bytes[0] == (byte) 0xFE
                && bytes[1] == (byte) 0xFF) {
                return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16BE"));
            } else if (bytes.length >= 2
                && bytes[0] == (byte) 0xFF
                && bytes[1] == (byte) 0xFE) {
                return new String(bytes, 2, bytes.length - 2, Charset.forName("UTF-16LE"));
            } else {
                /*
                 * Attempt to retrieve the default charset from the 'Content-Encoding' header,
                 * if the value isn't present or invalid fallback to 'UTF-8' for the default charset.
                 */
                if (contentType != null && contentType.length() != 0) {
                    try {
                        Matcher charsetMatcher = CHARSET_PATTERN.matcher(contentType);
                        if (charsetMatcher.find()) {
                            return new String(bytes, Charset.forName(charsetMatcher.group(1)));
                        } else {
                            return new String(bytes, Charset.forName("UTF-8"));
                        }
                    } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                        return new String(bytes, Charset.forName("UTF-8"));
                    }
                } else {
                    return new String(bytes, Charset.forName("UTF-8"));
                }
            }
        }
    }
}
