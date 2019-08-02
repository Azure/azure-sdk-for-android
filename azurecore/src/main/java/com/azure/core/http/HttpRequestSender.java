package com.azure.core.http;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

interface HttpRequestSender {
    Response send(Request request) throws IOException;
}
