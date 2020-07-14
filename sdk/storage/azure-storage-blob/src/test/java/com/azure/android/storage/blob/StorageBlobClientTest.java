package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackSimple;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobProperties;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.CommitBlockListOptions;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.GetBlobPropertiesOptions;
import com.azure.android.storage.blob.models.StageBlockOptions;
import com.azure.android.storage.blob.models.StageBlockResult;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

public class StorageBlobClientTest {
    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String BASE_URL = mockWebServer.url("/").toString();
    private static StorageBlobAsyncClient storageBlobAsyncClient = new StorageBlobAsyncClient.Builder("client.test.1",
        new ServiceClient.Builder()
            .setBaseUrl(BASE_URL)
            .setSerializationFormat(SerializerFormat.XML))
        .build();

    private static StorageBlobClient storageBlobClient = new StorageBlobClient.Builder(new ServiceClient.Builder()
            .setBaseUrl(BASE_URL)
            .setSerializationFormat(SerializerFormat.XML))
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
    public void getBlobsInPage() {
        // Given a StorageBlobClient.

        // When requesting a list of the blobs in a container using getBlobsInPage().
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        List<BlobItem> blobItems = storageBlobClient.getBlobsInPage(null,
            "testContainer",
            null);

        // Then a list containing the details of the blobs will be returned by the service and converted to BlobItem
        // objects by the client.
        assertNotEquals(0, blobItems.size());
        assertEquals("test.jpg", blobItems.get(0).getName());
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
            new Callback<List<BlobItem>>() {
                @Override
                public void onResponse(List<BlobItem> response) {
                    try {
                        // Then a list containing the details of the blobs will be returned to the callback by the service
                        // and converted to BlobItem objects by the client.
                        assertNotEquals(0, response.size());
                        assertEquals("test.jpg", response.get(0).getName());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
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

        ContainersListBlobFlatSegmentResponse response =
            storageBlobClient.getBlobsInPageWithRestResponse(null,
                "testContainer",
                null,
                null,
                null,
                null,
                null,
                CancellationToken.NONE);

        // Then the client will return an object that contains both the details of the REST response and a list
        // with the details of the blobs.
        List<BlobItem> blobItems = response.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : response.getValue().getSegment().getBlobItems();

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

        storageBlobAsyncClient.getBlobsInPageWithRestResponse(null,
            "testContainer",
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            new Callback<ContainersListBlobFlatSegmentResponse>() {
                @Override
                public void onResponse(ContainersListBlobFlatSegmentResponse response) {
                    try {
                        // Then the client will return an object that contains both the details of the REST response and
                        // a list with the details of the blobs to the callback.
                        List<BlobItem> blobItems = response.getValue().getSegment() == null
                            ? new ArrayList<>(0)
                            : response.getValue().getSegment().getBlobItems();

                        assertEquals(200, response.getStatusCode());
                        assertNotEquals(0, blobItems.size());
                        assertEquals("test.jpg", blobItems.get(0).getName());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
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
        BlobProperties blobProperties = storageBlobClient.getBlobProperties("container",
            "blob");

        assertEquals("application/text", blobProperties.getContentType());
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
            new CallbackSimple<BlobProperties>() {
                @Override
                public void onSuccess(BlobProperties value, Response response) {
                    try {
                        // Then an object with the blob properties will be returned by the client to the callback.
                        assertEquals("application/text", value.getContentType());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });


            awaitOnLatch(latch, "getBlobProperties");
    }

    @Test
    public void getBlobPropertiesWithOptions() {
        // Given a StorageBlobClient.

        // When requesting the properties of a blob using getBlobPropertiesAsHeaders().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        // Then the client will return an object that contains both the details of the REST response and
        // a an object with the blob properties.
        com.azure.android.core.http.Response<BlobProperties> restResponse =
            storageBlobClient.getBlobPropertiesWithRestResponse("container",
                "blob",
                new GetBlobPropertiesOptions().setCancellationToken(CancellationToken.NONE));

        assertEquals("application/text", restResponse.getValue().getContentType());
    }

    @Test
    public void getBlobPropertiesWithOptions_withCallback() {
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
            new GetBlobPropertiesOptions().setCancellationToken(CancellationToken.NONE),
            new CallbackSimple<BlobProperties>() {
                @Override
                public void onSuccess(BlobProperties value, Response response) {
                    try {
                        // Then the client will return an object that contains both the details of the REST response and
                        // an object with the blob properties to the callback.
                        assertEquals("application/text", value.getContentType());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "getBlobPropertiesWithRestResponse");
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
            new Callback<ResponseBody>() {
                @Override
                public void onResponse(ResponseBody response) {
                    try {
                        // Then an object with the blob's contents will be returned by the client.
                        assertEquals("testBody", response.string());
                    } catch (IOException e) {
                        onFailure(e);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
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

        storageBlobAsyncClient.rawDownloadWithRestResponse("testContainer",
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
            new Callback<BlobDownloadResponse>() {
                @Override
                public void onResponse(BlobDownloadResponse response) {
                    try {
                        // Then an object with the blob's contents will be returned by the client to the callback,
                        // including its properties and details from the REST response.
                        assertEquals(200, response.getStatusCode());
                        assertEquals("testBody", response.getValue().string());
                    } catch (IOException e) {
                        onFailure(e);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
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

        StageBlockResult stageBlockResult = storageBlobClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null);

        assertNull(stageBlockResult);
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
            new CallbackSimple<StageBlockResult>() {
                @Override
                public void onSuccess(StageBlockResult value, Response response) {
                    try {
                        assertNull(value);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });


        awaitOnLatch(latch, "stageBlock");
    }

    @Test
    public void stageBlockWithOptions() {
        // Given a StorageBlobClient.

        // When sending a block's contents for staging using stageBlockWithRestResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 201 will be returned by the server.
        com.azure.android.core.http.Response<StageBlockResult> response
            = storageBlobClient.stageBlockWithRestResponse("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            new StageBlockOptions().setCancellationToken(CancellationToken.NONE));

        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void stageBlockWithOptions_withCallback() {
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
            new StageBlockOptions(),
            new CallbackSimple<StageBlockResult>() {
                @Override
                public void onSuccess(StageBlockResult value, Response response) {
                    try {
                        // Then a response without body and status code 201 will be returned by the server to the callback.
                        assertEquals(201, response.code());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
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
            null);

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
            new CallbackSimple<BlockBlobItem>() {
                @Override
                public void onSuccess(BlockBlobItem value, Response response) {
                    try {
                        // Then a response with the blob's details and status code 201 will be returned by the server to
                        // the callback.
                        assertEquals(false, value.isServerEncrypted());
                        assertEquals("testEtag", value.getETag());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
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

        com.azure.android.core.http.Response<BlockBlobItem> response
            = storageBlobClient.commitBlockListWithRestResponse("testContainer",
            "testBlob",
            null,
            new CommitBlockListOptions().setCancellationToken(CancellationToken.NONE));

        assertEquals(false, response.getValue().isServerEncrypted());
        assertEquals("testEtag", response.getValue().getETag());
    }

    @Test
    public void commitBlockListWithOptions_withCallback() {
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
            new CommitBlockListOptions().setCancellationToken(CancellationToken.NONE), new CallbackSimple<BlockBlobItem>() {
                @Override
                public void onSuccess(BlockBlobItem value, Response response) {
                    try {
                        // Then a response with the blob's details and status code 201 will be returned by the server to
                        // the callback.
                        assertEquals(false, value.isServerEncrypted());
                        assertEquals("testEtag", value.getETag());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "commitBlockListWithRestResponse");
    }

    @Test
    public void delete() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        Void response = storageBlobClient.delete("container",
            "blob");

        assertNull(response);
    }

    @Test
    public void delete_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete() while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.delete("container",
            "blob",
            new Callback<Void>() {
                @Override
                public void onResponse(Void response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertNull(response);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "delete");
    }

    @Test
    public void deleteWithRestResponse() {
        // Given a StorageBlobClient.

        // When deleting a blob using deleteWithResponse().
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202);

        mockWebServer.enqueue(mockResponse);

        // Then a response without body and status code 202 will be returned by the server.
        BlobDeleteResponse response =
            storageBlobClient.deleteWithResponse("container",
                "blob",
                null,
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

        assertEquals(202, response.getStatusCode());
    }

    @Test
    public void deleteWithRestResponse_withCallback() {
        // Given a StorageBlobClient.

        // When deleting a blob using delete () while providing a callback.
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(202)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);

        storageBlobAsyncClient.deleteWithResponse("container",
            "blob",
            null,
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
            new Callback<BlobDeleteResponse>() {
                @Override
                public void onResponse(BlobDeleteResponse response) {
                    try {
                        // Then a response without body and status code 202 will be returned by the server to the callback.
                        assertEquals(202, response.getStatusCode());
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        throw new RuntimeException(t);
                    } finally {
                        latch.countDown();
                    }
                }
            });

        awaitOnLatch(latch, "deleteWithResponse");
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
