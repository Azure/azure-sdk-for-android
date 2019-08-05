package com.azure.core.http;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

class WrappedOkHttpRequest implements HttpRequest, UnwrapOkHttp.InnerRequest {
    private final Request inner;
    private HttpMethod method;
    private URL url;
    private HttpHeaders headers;
    private RequestBody body;

    WrappedOkHttpRequest(final Request inner) {
        this.inner = inner;
        this.headers = fromOkHttpHeaders(inner.headers());
        this.body = inner.body();
    }

    @Override
    public HttpMethod httpMethod() {
        return this.method != null ? this.method : httpMethod(this.inner.method());
    }

    @Override
    public HttpRequest httpMethod(HttpMethod httpMethod) {
        Objects.requireNonNull(httpMethod);
        this.method = httpMethod;
        return this;
    }

    @Override
    public URL url() {
        return this.url != null ? this.url : this.inner.url().url();
    }

    @Override
    public HttpRequest url(URL url) {
        Objects.requireNonNull(url);
        this.url = url;
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public HttpRequest headers(HttpHeaders headers) {
        Objects.requireNonNull(headers);
        this.headers = headers;
        return this;
    }

    @Override
    public HttpRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    @Override
    public Buffer body() throws IOException {
        if (this.body != null) {
            Buffer buffer = new Buffer();
            this.body.writeTo(buffer);
            return buffer;
        } else {
            return null;
        }
    }

    @Override
    public HttpRequest body(String content) {
        if (content == null) {
            this.body = null;
        } else {
            this.body = RequestBody.create(content.getBytes(StandardCharsets.UTF_8));
        }
        return this;
    }

    @Override
    public HttpRequest body(byte[] content) {
        if (content == null) {
            this.body = null;
        } else {
            this.body = RequestBody.create(content);
        }
        return this;
    }

    @Override
    public HttpRequest buffer() {
        return new WrappedOkHttpRequest(this.unwrap());
    }

    @Override
    public Request unwrap() {
        Request.Builder builder = this.inner.newBuilder();
        if (this.url != null) {
            builder.url(this.url);
        }
        builder.method(this.httpMethod().toString(), this.body);
        builder.headers(Headers.of(this.headers.toMap()));
        return builder.build();
    }

    private HttpMethod httpMethod(String s) {
        if (s.equalsIgnoreCase("GET")) {
            return HttpMethod.GET;
        }
        if (s.equalsIgnoreCase("HEAD")) {
            return HttpMethod.HEAD;
        }
        if (s.equalsIgnoreCase("POST")) {
            return HttpMethod.POST;
        }
        if (s.equalsIgnoreCase("PATCH")) {
            return HttpMethod.PATCH;
        }
        if (s.equalsIgnoreCase("PUT")) {
            return HttpMethod.PUT;
        }
        if (s.equalsIgnoreCase("DELETE")) {
            return HttpMethod.DELETE;
        }
        return null;
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
