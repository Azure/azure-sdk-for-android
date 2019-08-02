package com.azure.core;

import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;

import org.junit.Test;

import java.nio.charset.Charset;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import static junit.framework.TestCase.assertEquals;

public class RequestResponseSerializationTests {
    @Test
    public void test() throws Exception {
        MockWebServer server = new MockWebServer();

        HttpBinJSON httpBinJSONReq = new HttpBinJSON();
        httpBinJSONReq.url("wow");
        //
        // Schedule some responses.
        Buffer buffer = new Buffer();
        buffer.writeString(JacksonAdapter.createDefaultSerializerAdapter().serialize(httpBinJSONReq, SerializerEncoding.JSON), Charset.defaultCharset());
        server.enqueue(new MockResponse().setBody(buffer));

        server.start();

        HttpUrl baseUrl = server.url("/");
        HttpBinClient client = HttpBinClient.create(baseUrl.toString());

        HttpBinJSON httpBinJSONRes = client.getAnything();
        assertEquals("wow", httpBinJSONRes.url());

        RecordedRequest request1 = server.takeRequest();
        assertEquals("/anything", request1.getPath());
//        assertNotNull(request1.getHeader("Authorization"));
        server.shutdown();
    }
}
