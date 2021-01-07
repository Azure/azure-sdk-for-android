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
import com.azure.core.http.implementation.Util;
import com.azure.core.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class OkHttpAsyncHttpClient implements HttpClient {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClient.class);

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
    public void send(HttpRequest httpRequest, HttpCallback httpCallback) {
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
            final String contentType = httpRequest.getHeaders().getValue("Content-Type");
            if (contentType == null) {
                okhttpRequestBuilder.method(httpRequest.getHttpMethod().toString(),
                    RequestBody.create(null, httpRequest.getBody()));
            } else {
                okhttpRequestBuilder.method(httpRequest.getHttpMethod().toString(),
                    RequestBody.create(MediaType.parse(contentType), httpRequest.getBody()));
            }
        }

        final okhttp3.Request okHttpRequest = okhttpRequestBuilder.build();
        final Call call = httpClient.newCall(okHttpRequest);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException error) {
                httpCallback.onError(error);
            }

            @Override
            public void onResponse(Call call, Response response) {
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
                        return Util.bomAwareToString(this.getBodyAsByteArray(),
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
}
