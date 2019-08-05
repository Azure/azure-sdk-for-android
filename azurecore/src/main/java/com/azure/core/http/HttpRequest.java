package com.azure.core.http;

import java.io.IOException;
import java.net.URL;
import okio.Buffer;

public interface HttpRequest {
    HttpMethod httpMethod();

    HttpRequest httpMethod(HttpMethod httpMethod);

    URL url();

    HttpRequest url(URL url);

    HttpHeaders headers();

    HttpRequest headers(HttpHeaders headers);

    HttpRequest header(String name, String value);

    Buffer body() throws IOException;

    HttpRequest body(String content);

    HttpRequest body(byte[] content);

    HttpRequest buffer();
}
