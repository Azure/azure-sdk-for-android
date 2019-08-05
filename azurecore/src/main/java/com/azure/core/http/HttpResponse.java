package com.azure.core.http;

import java.io.Closeable;
import java.io.IOException;

public interface HttpResponse extends Closeable {
    int statusCode();
    String headerValue(String name);
    HttpHeaders headers();
    byte[] bodyAsByteArray() throws IOException;
    String bodyAsString() throws IOException;
    HttpRequest request();
    HttpResponse buffer();
}
