// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobGetTagsHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersHeaders;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersResponse;
import com.azure.android.storage.blob.models.BlobSetMetadataHeaders;
import com.azure.android.storage.blob.models.BlobSetMetadataResponse;
import com.azure.android.storage.blob.models.BlobSetTagsHeaders;
import com.azure.android.storage.blob.models.BlobSetTagsResponse;
import com.azure.android.storage.blob.models.BlobSetTierHeaders;
import com.azure.android.storage.blob.models.BlobSetTierResponse;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.ContainerCreateHeaders;
import com.azure.android.storage.blob.models.ContainerCreateResponse;
import com.azure.android.storage.blob.models.ContainerDeleteHeaders;
import com.azure.android.storage.blob.models.ContainerDeleteResponse;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StorageBlobClientTest {
    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String BASE_URL = mockWebServer.url("/").toString();
    private static StorageBlobAsyncClient storageBlobAsyncClient = new StorageBlobAsyncClient.Builder("client.test.1",
        new ServiceClient.Builder()
            .setBaseUrl(BASE_URL))
        .build();

    private static StorageBlobClient storageBlobClient = new StorageBlobClient.Builder(new ServiceClient.Builder()
            .setBaseUrl(BASE_URL))
        .build();

    @After
    public void tearDown() throws InterruptedException {
        // For ensuring the responses enqueued are consumed before making the next call.
        mockWebServer.takeRequest(20, TimeUnit.MILLISECONDS);
    }

    @Test
    public void newBuilder() {
        // Given a StorageBlobClient.

        // When creating another client based on the first one.
        StorageBlobAsyncClient otherStorageBlobAsyncClient = storageBlobAsyncClient.newBuilder("client.test.2").build();

        // Then the new client will contain the same properties as the original.
        assertEquals(storageBlobAsyncClient.getBlobServiceUrl(), otherStorageBlobAsyncClient.getBlobServiceUrl());
    }

    @Test
    public void getBlobServiceUrl() {
        assertEquals(storageBlobAsyncClient.getBlobServiceUrl(), BASE_URL);
    }

    @Test
    public void createContainer() {
        // Given a StorageBlobClient.

        // When creating a container using createContainer().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 201 will be returned by the server.
        Void response = storageBlobClient.createContainer("containerName");

        assertNull(response);
    }

    @Test
    public void createContainer_withCallback() {
        // Given a StorageBlobClient.

        // When creating a container using createContainer().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.createContainer("container",
            new CallbackWithHeader<Void, ContainerCreateHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerCreateHeaders header, Response response) {
                    try {
                        assertEquals(201, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "createContainer");
    }

    @Test
    public void createContainerWithRestResponse() {
        // Given a StorageBlobClient.

        // When creating a container using createContainer().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains the details of the REST response.
        ContainerCreateResponse response =
            storageBlobClient.createContainerWithRestResponse("container",
                null,
                null,
                null,
                CancellationToken.NONE);

        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void createContainerWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When creating a container using createContainer().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.createContainer("container",
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, ContainerCreateHeaders>() {

                @Override
                public void onSuccess(Void result, ContainerCreateHeaders header, Response response) {
                    try {
                        assertEquals(201, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable error, Response response) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "createContainer");
    }

    @Test
    public void getBlobsInPage() {
        // Given a StorageBlobClient.

        // When requesting a list of the blobs in a container using getBlobsInPage().
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        BlobsPage blobsPage = storageBlobClient.getBlobsInPage(null,
            "testContainer",
            null);

        // Then a list containing the details of the blobs will be returned by the service and converted to BlobItem
        // objects by the client.
        assertNotEquals(0, blobsPage.getItems()
            .size());
        assertEquals("test.jpg", blobsPage.getItems().get(0).getName());
    }

    @Test
    public void getBlobsInPage_withCallback() {
        // Given a StorageBlobClient.

        // When requesting a list of the blobs in a container using getBlobsInPage() while providing a callback.
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobsInPage(null,
            "testContainer",
            null,

            new Callback<BlobsPage>() {
                @Override
                public void onSuccess(BlobsPage result, Response response) {
                    try {
                        // Then a list containing the details of the blobs will be returned to the callback by the service
                        // and converted to BlobItem objects by the client.
                        assertNotEquals(0, result.getItems().size());
                        assertEquals("test.jpg", result.getItems().get(0).getName());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobsInPage");
    }

    @Test
    public void getBlobsInPageWithRestResponse() {
        // Given a StorageBlobClient.

        // When requesting a list of the blobs in a container using getBlobsInPageWithRestResponse().
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        com.azure.android.core.http.Response<BlobsPage> response =
            storageBlobClient.getBlobsInPageWithRestResponse(null,
                "testContainer",
                null,
                null,
                null,
                null,
                CancellationToken.NONE);

        // Then the client will return an object that contains both the details of the REST response and a list
        // with the details of the blobs.
        BlobsPage blobsPage = response.getValue();
        List<BlobItem> blobItems = blobsPage.getItems();

        assertEquals(200, response.getStatusCode());
        assertNotEquals(0, blobItems.size());
        assertEquals("test.jpg", blobItems.get(0).getName());
    }

    @Test
    public void getBlobsInPageWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When requesting a list of the blobs in a container using getBlobsInPageWithRestResponse() while providing a
        // callback.
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobsInPage(null,
            "testContainer",
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new Callback<BlobsPage>() {
                @Override
                public void onSuccess(BlobsPage result, Response response) {
                    try {
                        // Then the client will return an object that contains both the details of the REST response and
                        // a list with the details of the blobs to the callback.
                        List<BlobItem> blobItems = result.getItems();

                        assertEquals(200, response.code());
                        assertNotEquals(0, blobItems.size());
                        assertEquals("test.jpg", blobItems.get(0).getName());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobsInPageWithRestResponse");
    }

    @Test
    public void getBlobProperties() {
        // Given a StorageBlobClient.

        // When requesting the properties of a blob using getBlobProperties().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        // Then an object with the blob properties will be returned by the client.
        BlobGetPropertiesHeaders blobGetPropertiesHeaders = storageBlobClient.getBlobProperties("container",
            "blob");

        assertEquals("application/text", blobGetPropertiesHeaders.getContentType());
    }

    @Test
    public void getBlobProperties_withCallback() {
        // Given a StorageBlobClient.

        // When requesting the properties of a blob using getBlobProperties() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobProperties("container",
            "blob",
            new CallbackWithHeader<Void, BlobGetPropertiesHeaders>() {
                @Override
                public void onSuccess(Void result, BlobGetPropertiesHeaders header, Response response) {
                    try {
                        // Then an object with the blob properties will be returned by the client to the callback.
                        assertEquals("application/text", header.getContentType());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobProperties");
    }

    @Test
    public void getBlobPropertiesWithRestResponse() {
        // Given a StorageBlobClient.

        // When requesting the properties of a blob using getBlobPropertiesAsHeaders().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains both the details of the REST response and
        // a an object with the blob properties.
        BlobGetPropertiesResponse response =
            storageBlobClient.getBlobPropertiesWithRestResponse("container",
                "blob",
                null,
                null,
                null,
                null,
                CancellationToken.NONE);

        assertEquals("application/text", response.getDeserializedHeaders().getContentType());
    }

    @Test
    public void getBlobPropertiesWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When requesting the properties of a blob using getBlobPropertiesWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobProperties("container",
            "blob",
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobGetPropertiesHeaders>() {

                @Override
                public void onSuccess(Void result, BlobGetPropertiesHeaders header, Response response) {
                    try {
                        // Then the client will return an object that contains both the details of the REST response and
                        // an object with the blob properties to the callback.
                        assertEquals("application/text", header.getContentType());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable error, Response response) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobPropertiesWithRestResponse");
    }

    @Test
    public void setBlobHttpHeaders() {
        // Given a StorageBlobClient.

        // When setting the properties of a blob using setBlobHttpHeaders().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        // Then a void response be returned by the client.
        Void response = storageBlobClient.setBlobHttpHeaders("container",
            "blob", new BlobHttpHeaders());

        assertNull(response);
    }

    @Test
    public void setBlobHttpHeaders_withCallback() {
        // Given a StorageBlobClient.

        // When setting the properties of a blob using setBlobHttpHeaders() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.setBlobHttpHeaders("container",
            "blob", new BlobHttpHeaders(),
            new CallbackWithHeader<Void, BlobSetHttpHeadersHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetHttpHeadersHeaders header, Response response) {
                    try {
                        // Then an object with the return headers will be returned by the client to the callback.
                        assertEquals(200, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobHttpHeaders");
    }

    @Test
    public void setBlobHttpHeadersWithRestResponse() {
        // Given a StorageBlobClient.

        // When setting the properties of a blob using setBlobHttpHeadersWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains the details of the REST response.
        BlobSetHttpHeadersResponse response =
            storageBlobClient.setBlobHttpHeadersWithResponse("container",
                "blob",
                null,
                null,
                new BlobHttpHeaders(),
                CancellationToken.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void setBlobHttpHeadersWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When setting the properties of a blob using setBlobHttpHeadersWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.setBlobHttpHeaders("container",
            "blob",
            null,
            null,
            new BlobHttpHeaders(),
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobSetHttpHeadersHeaders>() {

                @Override
                public void onSuccess(Void result, BlobSetHttpHeadersHeaders header, Response response) {
                    try {
                        // Then the client will return an object that contains the details of the REST response
                        assertEquals(200, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable error, Response response) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobHttpHeadersWithRestResponse");
    }

    @Test
    public void setBlobMetadata() {
        // Given a StorageBlobClient.

        // When setting the metadata of a blob using setBlobMetadata().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        // Then a void response be returned by the client.
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        Void response = storageBlobClient.setBlobMetadata("container",
            "blob", metadata);

        assertNull(response);
    }

    @Test
    public void setBlobMetadata_withCallback() {
        // Given a StorageBlobClient.

        // When setting the metadata of a blob using setBlobMetadata() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        storageBlobAsyncClient.setBlobMetadata("container",
            "blob", metadata,
            new CallbackWithHeader<Void, BlobSetMetadataHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetMetadataHeaders header, Response response) {
                    try {
                        // Then an object with the return headers will be returned by the client to the callback.
                        assertEquals(200, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobMetadata");
    }

    @Test
    public void setBlobMetadataWithRestResponse() {
        // Given a StorageBlobClient.

        // When setting the metadata of a blob using setBlobMetadataWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains the details of the REST response.
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        BlobSetMetadataResponse response =
            storageBlobClient.setBlobMetadataWithResponse("container",
                "blob",
                null,
                null,
                metadata,
                null,
                CancellationToken.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void setBlobMetadataWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When setting the metadata of a blob using setBlobMetadataWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        storageBlobAsyncClient.setBlobMetadata("container",
            "blob",
            null,
            null,
            metadata,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobSetMetadataHeaders>() {

                @Override
                public void onSuccess(Void result, BlobSetMetadataHeaders header, Response response) {
                    try {
                        // Then the client will return an object that contains the details of the REST response
                        assertEquals(200, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable error, Response response) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobMetadataWithRestResponse");
    }

    @Test
    public void setBlobTier() {
        // Given a StorageBlobClient.

        // When setting the tier on a blob using setBlobTier().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        Void response = storageBlobClient.setBlobTier("container",
            "blob", AccessTier.HOT);

        assertNull(response);
    }

    @Test
    public void setBlobTier_withCallback() {
        // Given a StorageBlobClient.

        // When setting the tier on a blob using setBlobTier() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.setBlobTier("container",
            "blob",
            AccessTier.HOT,
            new CallbackWithHeader<Void, BlobSetTierHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetTierHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobTier");
    }

    @Test
    public void setBlobTierWithRestResponse() {
        // Given a StorageBlobClient.

        // When setting the tier on a blob using setTierWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        BlobSetTierResponse response =
            storageBlobClient.setBlobTierWithRestResponse("container",
                "blob",
                AccessTier.HOT,
                null,
                null,
                null,
                null,
                CancellationToken.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void setBlobTierWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When setting the tier on a blob using setTier() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.setBlobTier("container",
            "blob",
            AccessTier.HOT,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobSetTierHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetTierHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobTierWithResponse");
    }

    @Test
    public void download() throws IOException {
        // Given a StorageBlobClient.

        // When requesting to download the contents of a blob using download().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        // Then an object with the blob's contents will be returned by the client.
        ResponseBody response = storageBlobClient.rawDownload("testContainer",
            "testBlob");

        assertEquals("testBody", response.string());
    }

    @Test
    public void download_withCallback() {
        // Given a StorageBlobClient.

        // When requesting to download the contents of a blob using download() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.rawDownload("testContainer",
            "testBlob",

            new CallbackWithHeader<ResponseBody, BlobDownloadHeaders>() {
                @Override
                public void onSuccess(ResponseBody result, BlobDownloadHeaders header, Response response) {
                    try {
                        // Then an object with the blob's contents will be returned by the client.
                        assertEquals("testBody", result.string());
                    } catch (IOException e) {
                        onFailure(e, response);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "download");
    }

    @Test
    public void downloadWithRestResponse() throws IOException {
        // Given a StorageBlobClient.

        // When requesting to download the contents of a blob using downloadWithRestResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        // Then an object with the blob's contents will be returned by the client, including its properties and
        // details from the REST response.
        BlobDownloadResponse response = storageBlobClient.rawDownloadWithRestResponse("testContainer",
            "testBlob",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals("testBody", response.getValue().string());
    }

    @Test
    public void downloadWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When requesting to download the contents of a blob using downloadWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.rawDownload("testContainer",
            "testBlob",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<ResponseBody, BlobDownloadHeaders>() {
                @Override
                public void onSuccess(ResponseBody result, BlobDownloadHeaders header, Response response) {
                    try {
                        // Then an object with the blob's contents will be returned by the client to the callback,
                        // including its properties and details from the REST response.
                        assertEquals(200, response.code());
                        assertEquals("testBody", result.string());
                    } catch (IOException e) {
                        onFailure(e, response);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "downloadWithRestResponse");
    }

    @Test
    public void stageBlock() {
        // Given a StorageBlobClient.

        // When sending a block's contents for staging using stageBlock().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 201 will be returned by the server.
        Void response = storageBlobClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null);

        assertNull(response);
    }

    @Test
    public void stageBlock_withCallback() {
        // Given a StorageBlobClient.

        // When sending a block's contents for staging while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobStageBlockHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 201 will be returned by the server to the callback.
                        assertNull(response);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "stageBlock");
    }

    @Test
    public void stageBlockWithRestResponse() {
        // Given a StorageBlobClient.

        // When sending a block's contents for staging using stageBlockWithRestResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 201 will be returned by the server.
        BlockBlobsStageBlockResponse response = storageBlobClient.stageBlockWithRestResponse("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            null,
            false,
            null,
            null,
            null,
            CancellationToken.NONE);

        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void stageBlockWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When sending a block's contents for staging using stageBlockWithRestResponse() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            null,
            false,
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobStageBlockHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 201 will be returned by the server to the callback.
                        assertEquals(201, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "stageBlockWithRestResponse");
    }

    @Test
    public void commitBlockList() {
        // Given a StorageBlobClient.

        // When committing a list of blocks for upload using commitBlockList().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        // Then a response with the blob's details and status code 201 will be returned by the server.
        BlockBlobItem response = storageBlobClient.commitBlockList("testContainer",
            "testBlob",
            null,
            true);

        assertEquals(false, response.isServerEncrypted());
        assertEquals("testEtag", response.getETag());
    }

    @Test
    public void commitBlockList_withCallback() {
        // Given a StorageBlobClient.

        // When committing a list of blocks for upload using commitBlockList() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.commitBlockList("testContainer",
            "testBlob",
            null,
            true,

            new CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(BlockBlobItem result, BlockBlobCommitBlockListHeaders header, Response response) {
                    try {
                        // Then a response with the blob's details and status code 201 will be returned by the server to
                        // the callback.
                        assertEquals(false, result.isServerEncrypted());
                        assertEquals("testEtag", result.getETag());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "commitBlockList");
    }

    @Test
    public void commitBlockListWithRestResponse() {
        // Given a StorageBlobClient.

        // When committing a list of blocks for upload using commitBlockListWithRestResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        // Then a response with the blob's details and status code 201 will be returned by the server.
        BlockBlobsCommitBlockListResponse response = storageBlobClient.commitBlockListWithRestResponse("testContainer",
            "testBlob",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE);

        assertEquals(false, response.getBlockBlobItem().isServerEncrypted());
        assertEquals("testEtag", response.getBlockBlobItem().getETag());
    }

    @Test
    public void commitBlockListWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When committing a list of blocks for upload using commitBlockListWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.commitBlockList("testContainer",
            "testBlob",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,

            new CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(BlockBlobItem result, BlockBlobCommitBlockListHeaders header, Response response) {
                    try {
                        // Then a response with the blob's details and status code 201 will be returned by the server to
                        // the callback.
                        assertEquals(false, result.isServerEncrypted());
                        assertEquals("testEtag", result.getETag());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "commitBlockListWithRestResponse");
    }

    @Test
    public void deleteBlob() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        Void response = storageBlobClient.deleteBlob("container",
            "blob");

        assertNull(response);
    }

    @Test
    public void deleteBlob_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.deleteBlob("container",
            "blob",
            new CallbackWithHeader<Void, BlobDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, BlobDeleteHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertNull(response);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "delete");
    }

    @Test
    public void deleteBlobWithRestResponse() {
        // Given a StorageBlobClient.

        // When deleting a blob using deleteWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        BlobDeleteResponse response =
            storageBlobClient.deleteBlobWithRestResponse("container",
                "blob",
                null,
                null,
                null,
                null,
                CancellationToken.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void deleteBlobWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete () while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.deleteBlob("container",
            "blob",
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, BlobDeleteHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "deleteWithResponse");
    }

    @Test
    public void deleteContainer() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        Void response = storageBlobClient.deleteContainer("container");

        assertNull(response);
    }

    @Test
    public void deleteContainer_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.deleteContainer("container",
            new CallbackWithHeader<Void, ContainerDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerDeleteHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "deleteContainer");
    }

    @Test
    public void deleteContainerWithRestResponse() {
        // Given a StorageBlobClient.

        // When deleting a container using deleteWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        ContainerDeleteResponse response =
            storageBlobClient.deleteContainerWithRestResponse("container",
                null,
                null,
                CancellationToken.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void deleteContainerWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a container using deleteContainer() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.deleteContainer("container",
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, ContainerDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerDeleteHeaders header, Response response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "deleteContainer");
    }

    @Test
    public void getBlobTags() {
        // Given a StorageBlobClient.

        // When requesting the tags of the blobs using getBlobTags().
        String responseBody = readFileToString("GetTagsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        Map<String, String> blobTags = storageBlobClient.getBlobTags("testContainer",
            "testBlob");

        // Then a map containing the details of the blob tags will be returned by the service and converted to a Map
        // by the client.
        assertNotEquals(0, blobTags.size());
        assertTrue(blobTags.containsKey("tag0"));
        assertEquals("tag0value", blobTags.get("tag0"));
        assertTrue(blobTags.containsKey("tag1"));
        assertEquals("tag1value", blobTags.get("tag1"));
    }

    @Test
    public void getBlobTags_withCallback() {
        // Given a StorageBlobClient.

        // When requesting the tags of the blobs using getBlobTags() while providing a callback.
        String responseBody = readFileToString("GetTagsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobTags("testContainer",
            "testBlob",
            new CallbackWithHeader<Map<String, String>, BlobGetTagsHeaders>() {

                @Override
                public void onSuccess(Map<String, String> result, BlobGetTagsHeaders header, Response response) {
                    try {
                        // Then a map containing the details of the blob tags will be returned by the service and converted to a Map
                        // by the client.
                        assertEquals(200, response.code());
                        assertNotEquals(0, result.size());
                        assertTrue(result.containsKey("tag0"));
                        assertEquals("tag0value", result.get("tag0"));
                        assertTrue(result.containsKey("tag1"));
                        assertEquals("tag1value", result.get("tag1"));
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobTags");
    }

    @Test
    public void getBlobTagsWithRestResponse() {
        // Given a StorageBlobClient.

        // When requesting the tags of the blobs using getBlobTagsWithRestResponse() while providing a callback.
        String responseBody = readFileToString("GetTagsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        com.azure.android.core.http.Response<Map<String, String>> response =
            storageBlobClient.getBlobTagsWithRestResponse("testContainer",
                "blobName",
                null,
                null,
                CancellationToken.NONE);

        // Then a map containing the details of the blob tags will be returned by the service and converted to a Map
        // by the client.
        Map<String, String> result = response.getValue();

        assertEquals(200, response.getStatusCode());
        assertNotEquals(0, result.size());
        assertTrue(result.containsKey("tag0"));
        assertEquals("tag0value", result.get("tag0"));
        assertTrue(result.containsKey("tag1"));
        assertEquals("tag1value", result.get("tag1"));
    }

    @Test
    public void getBlobTagsWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When requesting the tags of the blobs using getBlobTagsWithRestResponse() while providing a callback.
        String responseBody = readFileToString("GetTagsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.getBlobTags("testContainer",
            "testBlob",
            null,
            null,
            CancellationToken.NONE,
            new CallbackWithHeader<Map<String, String>, BlobGetTagsHeaders>() {

                @Override
                public void onSuccess(Map<String, String> result, BlobGetTagsHeaders header, Response response) {
                    try {
                        // Then a map containing the details of the blob tags will be returned by the service and converted to a Map
                        // by the client.
                        assertEquals(200, response.code());
                        assertNotEquals(0, result.size());
                        assertTrue(result.containsKey("tag0"));
                        assertEquals("tag0value", result.get("tag0"));
                        assertTrue(result.containsKey("tag1"));
                        assertEquals("tag1value", result.get("tag1"));
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobTags");
    }

    @Test
    public void setBlobTags() {
        // Given a StorageBlobClient.

        // When setting the tags of a blob using setBlobTags().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(204);

        mockWebServer.enqueue(mockResponse);

        // Then a void response be returned by the client.
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        Void response = storageBlobClient.setBlobTags("container",
            "blob", tags);

        assertNull(response);
    }

    @Test
    public void setBlobTags_withCallback() {
        // Given a StorageBlobClient.

        // When setting the tags of a blob using setBlobTags() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(204);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        storageBlobAsyncClient.setBlobTags("container",
            "blob", tags,
            new CallbackWithHeader<Void, BlobSetTagsHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetTagsHeaders header, Response response) {
                    try {
                        // Then an object with the return headers will be returned by the client to the callback.
                        assertEquals(204, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    try {
                        throw new RuntimeException(throwable);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobTags");
    }

    @Test
    public void setBlobTagsWithRestResponse() {
        // Given a StorageBlobClient.

        // When setting the tags of a blob using setBlobTagsWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(204);

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains the details of the REST response.
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        BlobSetTagsResponse response =
            storageBlobClient.setBlobTagsWithResponse("container",
                "blob",
                null,
                null,
                tags,
                CancellationToken.NONE);

        assertEquals(204, response.getStatusCode());
    }

    @Test
    public void setBlobTagsWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When setting the tags of a blob using setBlobTagsWithRestResponse() while providing a
        // callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(204);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        storageBlobAsyncClient.setBlobTags("container",
            "blob",
            null,
            null,
            tags,
            CancellationToken.NONE,
            new CallbackWithHeader<Void, BlobSetTagsHeaders>() {

                @Override
                public void onSuccess(Void result, BlobSetTagsHeaders header, Response response) {
                    try {
                        // Then the client will return an object that contains the details of the REST response
                        assertEquals(204, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable error, Response response) {
                    try {
                        throw new RuntimeException(error);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "setBlobTags");
    }

    private static String readFileToString(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream =
                 Files.lines(Paths.get("src", "test", "resources", filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    private static void awaitOnLatch(CountDownLatch latch, String method) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            assertFalse(method + " didn't produce any result.", true);
        }
    }
}
