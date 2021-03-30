// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.core.rest;

import com.azure.android.core.http.HttpCallDispatcher;
import com.azure.android.core.http.HttpCallback;
import com.azure.android.core.http.HttpClient;
import com.azure.android.core.http.HttpHeaders;
import com.azure.android.core.http.HttpMethod;
import com.azure.android.core.http.HttpRequest;
import com.azure.android.core.http.HttpResponse;
import com.azure.android.core.serde.jackson.SerdeEncoding;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.http.HttpPipeline;
import com.azure.android.core.http.HttpPipelineBuilder;
import com.azure.android.core.rest.annotation.BodyParam;
import com.azure.android.core.rest.annotation.Get;
import com.azure.android.core.rest.annotation.Host;
import com.azure.android.core.rest.annotation.Put;
import com.azure.android.core.rest.annotation.ServiceInterface;
import com.azure.android.core.serde.jackson.JacksonSerder;

import org.junit.jupiter.api.Test;
import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RestProxyXMLTests {
    static class MockXMLHTTPClient implements HttpClient {
        @Override
        public HttpCallDispatcher getHttpCallDispatcher() {
            return new HttpCallDispatcher();
        }

        @Override
        public void send(HttpRequest request, CancellationToken cancellationToken, HttpCallback httpCallback) {
            try {
                if (request.getUrl().toString().endsWith("GetContainerACLs")) {
                    httpCallback.onSuccess(response(request, "GetContainerACLs.xml"));
                } else if (request.getUrl().toString().endsWith("GetXMLWithAttributes")) {
                    httpCallback.onSuccess(response(request, "GetXMLWithAttributes.xml"));
                } else {
                    httpCallback.onSuccess(new MockHttpResponse(request, 404));
                }
            } catch (IOException | URISyntaxException e) {
                httpCallback.onError(e);
            }
        }

        private HttpResponse response(HttpRequest request, String resource) throws IOException, URISyntaxException {
            URL url = getClass().getClassLoader().getResource(resource);
            byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
            HttpHeaders headers = new HttpHeaders().put("Content-Type", "application/xml");
            HttpResponse res = new MockHttpResponse(request, 200, headers, bytes);
            return res;
        }
    }

    @Host("http://unused")
    @ServiceInterface(name = "MyXMLService")
    interface MyXMLService {
        @Get("GetContainerACLs")
        void getContainerACLs(Callback<Response<SignedIdentifiersWrapper>> callback);

        @Put("SetContainerACLs")
        void setContainerACLs(@BodyParam("application/xml") SignedIdentifiersWrapper signedIdentifiers,
                              Callback<Response<Void>> callback);
    }

    private static class SignedIdentifiersWrapperOrError {
        public SignedIdentifiersWrapper wrapper;
        public Throwable error;
    }

    @Test
    public void canReadXMLResponse() {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockXMLHTTPClient())
            .build();

        MyXMLService myXMLService = RestProxy.create(MyXMLService.class,
            pipeline,
            new JacksonSerder());

        final SignedIdentifiersWrapperOrError wrapperOrError = new SignedIdentifiersWrapperOrError();

        CountDownLatch latch = new CountDownLatch(1);
        myXMLService.getContainerACLs(
            new Callback<Response<SignedIdentifiersWrapper>>() {
            @Override
            public void onSuccess(Response<SignedIdentifiersWrapper> response) {
                try {
                    wrapperOrError.wrapper = response.getValue();
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                try {
                    wrapperOrError.error = error;
                } finally {
                    latch.countDown();
                }
            }
        });

        awaitOnLatch(latch, "canReadXMLResponse");

        if (wrapperOrError.error != null) {
            fail(wrapperOrError.error);
        } else {
            assertNotNull(wrapperOrError.wrapper);
            assertNotNull(wrapperOrError.wrapper.signedIdentifiers());
            assertNotEquals(0, wrapperOrError.wrapper.signedIdentifiers().size());
        }
    }

    @Host("http://unused")
    @ServiceInterface(name = "MyXMLServiceWithAttributes")
    public interface MyXMLServiceWithAttributes {
        @Get("GetXMLWithAttributes")
        void getSlideshow(Callback<Response<Slideshow>> callback);
    }

    private static class SlideshowOrError {
        public Slideshow slideshow;
        public Throwable error;
    }

    @Test
    public void canDeserializeXMLWithAttributes() throws Exception {
        JacksonSerder serializer = new JacksonSerder();

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new MockXMLHTTPClient())
            .build();

        MyXMLServiceWithAttributes myXMLService = RestProxy.create(
            MyXMLServiceWithAttributes.class,
            pipeline,
            serializer);

        final SlideshowOrError slideshowOrError = new SlideshowOrError();

        CountDownLatch latch = new CountDownLatch(1);
        myXMLService.getSlideshow(new Callback<Response<Slideshow>>() {
            @Override
            public void onSuccess(Response<Slideshow> response) {
                try {
                    slideshowOrError.slideshow = response.getValue();
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                try {
                    slideshowOrError.error = error;
                } finally {
                    latch.countDown();
                }
            }
        });

        awaitOnLatch(latch, "canDeserializeXMLWithAttributes");

        if (slideshowOrError.error != null) {
            fail(slideshowOrError.error);
        } else {
            Slideshow slideshow = slideshowOrError.slideshow;
            assertEquals("Sample Slide Show", slideshow.title());
            assertEquals("Date of publication", slideshow.date());
            assertEquals("Yours Truly", slideshow.author());
            assertEquals(2, slideshow.slides().length);

            assertEquals("all", slideshow.slides()[0].type());
            assertEquals("Wake up to WonderWidgets!", slideshow.slides()[0].title());
            assertEquals(0, slideshow.slides()[0].items().length);

            assertEquals("all", slideshow.slides()[1].type());
            assertEquals("Overview", slideshow.slides()[1].title());
            assertEquals(3, slideshow.slides()[1].items().length);
            assertEquals("Why WonderWidgets are great", slideshow.slides()[1].items()[0]);
            assertEquals("", slideshow.slides()[1].items()[1]);
            assertEquals("Who buys WonderWidgets", slideshow.slides()[1].items()[2]);

            String xml = serializer.serialize(slideshow, SerdeEncoding.XML);
            Slideshow newSlideshow = serializer.deserialize(xml, Slideshow.class, SerdeEncoding.XML);
            String newXML = serializer.serialize(newSlideshow, SerdeEncoding.XML);
            assertEquals(xml, newXML);
        }
    }

    static class MockXMLReceiverClient implements HttpClient {
        byte[] receivedBytes = null;

        @Override
        public HttpCallDispatcher getHttpCallDispatcher() {
            return new HttpCallDispatcher();
        }

        @Override
        public void send(HttpRequest request, CancellationToken cancellationToken, HttpCallback httpCallback) {
            if (request.getUrl().toString().endsWith("SetContainerACLs")) {
                this.receivedBytes = request.getBody();
                httpCallback.onSuccess(new MockHttpResponse(request, 200));
            } else {
                httpCallback.onSuccess(new MockHttpResponse(request, 404));
            }
        }
    }

    @Test
    public void canWriteXMLRequest() throws Exception {
        URL url = getClass().getClassLoader().getResource("GetContainerACLs.xml");
        byte[] bytes = Files.readAllBytes(Paths.get(url.toURI()));
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "http://unused/SetContainerACLs");
        request.setBody(bytes);

        SignedIdentifierInner si = new SignedIdentifierInner();
        si.withId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");

        AccessPolicy ap = new AccessPolicy();
        ap.withStart(OffsetDateTime.parse("2009-09-28T08:49:37.0000000Z"));
        ap.withExpiry(OffsetDateTime.parse("2009-09-29T08:49:37.0000000Z"));
        ap.withPermission("rwd");

        si.withAccessPolicy(ap);
        List<SignedIdentifierInner> expectedAcls = Collections.singletonList(si);

        JacksonSerder serderAdapter = new JacksonSerder();
        MockXMLReceiverClient httpClient = new MockXMLReceiverClient();

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .build();

        MyXMLService myXMLService = RestProxy.create(MyXMLService.class,
            pipeline,
            serderAdapter);
        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(expectedAcls);

        CountDownLatch latch = new CountDownLatch(1);
        myXMLService.setContainerACLs(wrapper, new Callback<Response<Void>>() {
            @Override
            public void onSuccess(Response<Void> response) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable error) {
                latch.countDown();
            }
        });

        awaitOnLatch(latch, "canWriteXMLRequest");

        SignedIdentifiersWrapper actualAclsWrapped = serderAdapter.deserialize(
            new String(httpClient.receivedBytes, StandardCharsets.UTF_8),
            SignedIdentifiersWrapper.class,
            SerdeEncoding.XML);

        List<SignedIdentifierInner> actualAcls = actualAclsWrapped.signedIdentifiers();

        // Ideally we'd just check for "things that matter" about the XML-- e.g. the tag names, structure, and attributes needs to be the same,
        // but it doesn't matter if one document has a trailing newline or has UTF-8 in the header instead of utf-8, or if comments are missing.
        assertEquals(expectedAcls.size(), actualAcls.size());
        assertEquals(expectedAcls.get(0).id(), actualAcls.get(0).id());
        assertEquals(expectedAcls.get(0).accessPolicy().expiry(), actualAcls.get(0).accessPolicy().expiry());
        assertEquals(expectedAcls.get(0).accessPolicy().start(), actualAcls.get(0).accessPolicy().start());
        assertEquals(expectedAcls.get(0).accessPolicy().permission(), actualAcls.get(0).accessPolicy().permission());
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(500, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(true, method + " didn't produce any result.");
        }
    }
}
