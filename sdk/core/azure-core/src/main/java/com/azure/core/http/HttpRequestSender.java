package com.azure.core.http;

import java.io.IOException;

interface HttpRequestSender {
    HttpResponse send(HttpRequest request) throws IOException;
}
