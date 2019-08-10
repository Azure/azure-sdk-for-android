package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import java.io.IOException;

import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

public class OkHttpBufferedResponse implements HttpResponse, UnwrapOkHttp.InnerResponse {
    private final OkHttpResponse source;
    private Buffer bufferedContent;

    public OkHttpBufferedResponse(OkHttpResponse source) {
        this.source = source;
    }

    @Override
    public int statusCode() {
        return this.source.statusCode();
    }

    @Override
    public String headerValue(String name) {
        return this.headers().value(name);
    }

    @Override
    public HttpHeaders headers() {
        return this.source.headers();
    }

    @Override
    public byte[] bodyAsByteArray() throws IOException {
        if (this.bufferedContent == null && this.source.inner().body() != null) {
            BufferedSource bufferedSource = this.source.inner().body().source();
            bufferedSource.request(Long.MAX_VALUE);
            this.bufferedContent = bufferedSource.getBuffer();
        }
        return this.bufferedContent != null
            ? this.bufferedContent.clone().readByteArray()
            : null;
    }

    @Override
    public String bodyAsString() throws IOException {
        byte[] bytes = this.bodyAsByteArray();
        return bytes == null ? null : new String(bytes);
    }

    @Override
    public HttpRequest request() {
        return source.request();
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }

    @Override
    public void close() {
        source.close();
    }

    @Override
    public Response unwrap() {
        return this.source.inner();
    }
}
