// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.http.okhttp;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class OkHttpAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClient.class);
    private static final Pattern CHARSET_PATTERN
        = Pattern.compile("charset=([\\S]+)\\b", Pattern.CASE_INSENSITIVE);

    private final OkHttpClient httpClient;
    private final HttpCallDispatcher httpCallDispatcher;


    OkHttpAsyncHttpClient(OkHttpClient okHttpClient, HttpCallDispatcher httpCallDispatcher) {
        this.httpClient = okHttpClient;
        this.httpCallDispatcher = httpCallDispatcher;
    }

    @Override
    public HttpCallDispatcher getHttpCallDispatcher() {
        return this.httpCallDispatcher;
    }

    @Override
    public void send(HttpRequest httpRequest, CancellationToken cancellationToken, HttpCallback httpCallback) {
        okhttp3.Request.Builder okhttpRequestBuilder = new okhttp3.Request.Builder();

        okhttpRequestBuilder.url(httpRequest.getUrl());

        if (httpRequest.getHeaders() != null) {
            Map<String, String> headers = new HashMap<>();
            for (HttpHeader hdr : httpRequest.getHeaders()) {
                if (hdr.getValue() != null) {
                    headers.put(hdr.getName(), hdr.getValue());
                }
            }
            okhttpRequestBuilder.headers(okhttp3.Headers.of(headers));
        } else {
            okhttpRequestBuilder.headers(okhttp3.Headers.of(new HashMap<>()));
        }

        if (httpRequest.getHttpMethod() == HttpMethod.GET) {
            okhttpRequestBuilder.get();
        } else if (httpRequest.getHttpMethod() == HttpMethod.HEAD) {
            okhttpRequestBuilder.head();
        } else {
            byte[] content = httpRequest.getBody();
            content = content == null ? new byte[0] : content;

            final String contentType = httpRequest.getHeaders().getValue("Content-Type");
            if (contentType == null) {
                okhttpRequestBuilder.method(httpRequest.getHttpMethod().toString(),
                    RequestBody.create(null, content));
            } else {
                okhttpRequestBuilder.method(httpRequest.getHttpMethod().toString(),
                    RequestBody.create(MediaType.parse(contentType), content));
            }
        }

        final okhttp3.Request okHttpRequest = okhttpRequestBuilder.build();
        final okhttp3.Call call = httpClient.newCall(okHttpRequest);

        final String onCancelId = (cancellationToken == CancellationToken.NONE) ? null : UUID.randomUUID().toString();
        if (onCancelId != null) {
            // Register an identifiable Runnable to run on cancellationToken.cancel().
            //
            // This Runnable unregistered once the 'call' completes.
            //
            // We don't want a cancel on cancellationToken to call call.cancel()
            // after the call completion (though call.cancel() after it's completion is nop).
            //
            cancellationToken.registerOnCancel(onCancelId, () -> call.cancel());
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException error) {
                if (onCancelId != null) {
                    cancellationToken.unregisterOnCancel(onCancelId);
                }
                httpCallback.onError(error);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) {
                if (onCancelId != null) {
                    cancellationToken.unregisterOnCancel(onCancelId);
                }
                httpCallback.onSuccess(new HttpResponse(httpRequest) {
                    private final HttpHeaders headers = fromOkHttpHeaders(response.headers());
                    private final ResponseBody responseBody = response.body();

                    @Override
                    public int getStatusCode() {
                        return response.code();
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
                        if (this.responseBody == null) {
                            return new ByteArrayInputStream(new byte[0]);
                        } else {
                            return this.responseBody.byteStream();
                        }
                    }

                    @Override
                    public byte[] getBodyAsByteArray() {
                        if (this.responseBody == null) {
                            return new byte[0];
                        } else {
                            try {
                                return this.responseBody.bytes();
                            } catch (IOException e) {
                                throw logger.logExceptionAsError(new RuntimeException(e));
                            }
                        }
                    }

                    @Override
                    public String getBodyAsString() {
                        return bomAwareToString(this.getBodyAsByteArray(),
                            headers.getValue("Content-Type"));
                    }

                    @Override
                    public void close() {
                        if (this.responseBody != null) {
                            this.responseBody.close();
                        }
                    }

                    @Override
                    public String getBodyAsString(Charset charset) {
                        return new String(this.getBodyAsByteArray(), charset);
                    }

                    private HttpHeaders fromOkHttpHeaders(Headers headers) {
                        HttpHeaders httpHeaders = new HttpHeaders();
                        for (String headerName : headers.names()) {
                            httpHeaders.put(headerName, headers.get(headerName));
                        }
                        return httpHeaders;
                    }
                });
            }
        });
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
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
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
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        } else if (bytes.length >= 2
            && bytes[0] == (byte) 0xFF
            && bytes[1] == (byte) 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
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
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            } else {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }
}
