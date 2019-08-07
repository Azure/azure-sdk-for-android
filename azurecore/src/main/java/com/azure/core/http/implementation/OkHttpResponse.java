package com.azure.core.http.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import java.io.IOException;

import okhttp3.Response;

public class OkHttpResponse implements HttpResponse, UnwrapOkHttp.InnerResponse {
    private final okhttp3.Response inner;
    private final HttpRequest request;
    private final HttpHeaders headers;

    public OkHttpResponse(okhttp3.Response inner, HttpRequest request) {
        this.inner = inner;
        this.request = request;
        this.headers = fromOkHttpHeaders(this.inner.headers());
    }

    @Override
    public int statusCode() {
        return this.inner.code();
    }

    @Override
    public String headerValue(String name) {
        return this.headers.value(name);
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public byte[] bodyAsByteArray() throws IOException {
        return this.inner.body() != null
            ? this.inner.body().bytes()
            : null;
    }

    @Override
    public String bodyAsString() throws IOException {
        return this.inner.body() != null
            ? this.inner.body().string()
            : null;
    }

    @Override
    public HttpRequest request() {
        return this.request;
    }

    @Override
    public HttpResponse buffer() {
        return new OkHttpBufferedResponse(this);
    }

    @Override
    public void close() {
        if (this.inner.body() != null) {
            this.inner.body().close();
        }
    }

    @Override
    public Response unwrap() {
        return this.inner;
    }

    Response inner() {
        return this.inner;
    }

    private static HttpHeaders fromOkHttpHeaders(okhttp3.Headers headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        for (String headerName : headers.names()) {
            httpHeaders.put(headerName, headers.get(headerName));
        }
        return httpHeaders;
    }
}
