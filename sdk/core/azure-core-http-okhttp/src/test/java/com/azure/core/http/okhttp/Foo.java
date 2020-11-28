package com.azure.core.http.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Foo {
    @Test
    public void bar() throws IOException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .build();
        Request request = new Request.Builder()
            .url("https://httpbin.org/#/HTTP_Methods/get_get")
            .get()
            .build();
        Response response = httpClient.newCall(request).execute();
        System.out.println(response.code());
    }
}
