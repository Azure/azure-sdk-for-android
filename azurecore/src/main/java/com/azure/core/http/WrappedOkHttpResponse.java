package com.azure.core.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

class WrappedOkHttpResponse implements HttpResponse, UnwrapOkHttp.InnerResponse {
    protected final Response inner;
    private final HttpRequest request;
    private final HttpHeaders headers;

    WrappedOkHttpResponse(Response response, HttpRequest request) {
        this.inner = response;
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
        return new WrappedOkHttpBufferedResponse(this);
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

    private static HttpHeaders fromOkHttpHeaders(okhttp3.Headers headers) {
        Map<String, List<String>> map = headers.toMultimap();
        HttpHeaders httpHeader = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            List<String> values = entry.getValue();
            httpHeader.put(entry.getKey(), values.get(values.size() - 1));
        }
        return httpHeader;
    }
}
