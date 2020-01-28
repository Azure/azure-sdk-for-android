package com.azure.android.core.http.interceptor;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static com.azure.android.core.http.interceptor.RequestIdInterceptor.REQUEST_ID_HEADER;

public class RequestIdInterceptorTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    @Test
    public void requestIdHeaderIsPopulated() throws IOException {
        server.enqueue(new MockResponse());

        final String[] requestId = {null};
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        httpClientBuilder.addInterceptor(new RequestIdInterceptor());
        httpClientBuilder.addInterceptor(chain -> {
            requestId[0] = (chain.request().header(REQUEST_ID_HEADER));

            return chain.proceed(chain.request());
        });

        OkHttpClient httpClient = httpClientBuilder.build();
        Request request = new okhttp3.Request.Builder()
            .url(getEndpointFrom(server))
            .build();

        httpClient.newCall(request).execute();

        Assert.assertNotNull(requestId[0]);
    }

    private static URL getEndpointFrom(MockWebServer server) throws MalformedURLException {
        try {
            return new URL(String.format("http://%s:%s", server.getHostName(), server.getPort()));
        } catch (MalformedURLException e) {
            e.printStackTrace();

            throw e;
        }
    }
}
