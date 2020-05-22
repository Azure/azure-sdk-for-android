package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceCall;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class StorageBlobClientTest {
    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final String BASE_URL = mockWebServer.url("/").toString();
    private static StorageBlobClient storageBlobClient = new StorageBlobClient.Builder(
        new ServiceClient.Builder()
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
        StorageBlobClient otherStorageBlobClient = storageBlobClient.newBuilder().build();

        assertEquals(storageBlobClient.getBlobServiceUrl(), otherStorageBlobClient.getBlobServiceUrl());
    }

    @Test
    public void getBlobServiceUrl() {
        assertEquals(storageBlobClient.getBlobServiceUrl(), BASE_URL);
    }

    @Test
    public void getBlobsInPage() {
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        List<BlobItem> blobItems = storageBlobClient.getBlobsInPage(null,
            "testContainer",
            null);

        assertEquals("test.jpg", blobItems.get(0).getName());
    }

    @Test
    public void getBlobsInPage_withCallback() {
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.getBlobsInPage(null,
            "testContainer",
            null,
            new Callback<List<BlobItem>>() {
                @Override
                public void onResponse(List<BlobItem> response) {
                    assertEquals("test.jpg", response.get(0).getName());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void getBlobsInPageWithRestResponse() {
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
                null);

        List<BlobItem> blobItems = response.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : response.getValue().getSegment().getBlobItems();

        assertEquals(200, response.getStatusCode());
        assertEquals("test.jpg", blobItems.get(0).getName());
    }

    @Test
    public void getBlobsInPageWithRestResponse_withCallback() {
        String responseBody = readFileToString("ListBlobsResponse.xml");

        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(responseBody);

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.getBlobsInPageWithRestResponse(null,
            "testContainer",
            null,
            null,
            null,
            null,
            null,
            new Callback<ContainersListBlobFlatSegmentResponse>() {
                @Override
                public void onResponse(ContainersListBlobFlatSegmentResponse response) {
                    List<BlobItem> blobItems = response.getValue().getSegment() == null
                        ? new ArrayList<>(0)
                        : response.getValue().getSegment().getBlobItems();

                    assertEquals(200, response.getStatusCode());
                    assertEquals("test.jpg", blobItems.get(0).getName());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void getBlobProperties() {
        MockResponse response = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(response);

        BlobDownloadHeaders blobDownloadHeaders = storageBlobClient.getBlobProperties("container",
            "blob");

        assertEquals("application/text", blobDownloadHeaders.getContentType());
    }

    @Test
    public void getBlobProperties_withCallback() {
        MockResponse response = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(response);

        ServiceCall serviceCall = storageBlobClient.getBlobProperties("container",
            "blob",
            new Callback<BlobDownloadHeaders>() {
                @Override
                public void onResponse(BlobDownloadHeaders response) {
                    assertEquals("application/text", response.getContentType());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void getBlobPropertiesAsHeaders() {
        MockResponse response = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(response);

        BlobDownloadHeaders blobDownloadHeaders = storageBlobClient.getBlobPropertiesAsHeaders("container",
            "blob",
            null,
            null,
            null,
            null,
            null,
            null);

        assertEquals("application/text", blobDownloadHeaders.getContentType());
    }

    @Test
    public void getBlobPropertiesAsHeaders_withCallback() {
        MockResponse response = new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/text");

        mockWebServer.enqueue(response);

        ServiceCall serviceCall = storageBlobClient.getBlobPropertiesAsHeaders("container",
            "blob",
            null,
            null,
            null,
            null,
            null,
            null,
            new Callback<BlobDownloadHeaders>() {
                @Override
                public void onResponse(BlobDownloadHeaders response) {
                    assertEquals("application/text", response.getContentType());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void download() throws IOException {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        BlobDownloadAsyncResponse response = storageBlobClient.download(
            "testContainer",
            "testBlob");

        assertEquals(200, response.getStatusCode());
        assertEquals("testBody", response.getValue().string());
    }

    @Test
    public void download_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.download("testContainer",
            "testBlob",
            new Callback<BlobDownloadAsyncResponse>() {
                @Override
                public void onResponse(BlobDownloadAsyncResponse response) {
                    try {
                        assertEquals(200, response.getStatusCode());
                        assertEquals("testBody", response.getValue().string());
                    } catch (IOException e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void downloadWithHeaders() throws IOException {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        BlobDownloadAsyncResponse response = storageBlobClient.downloadWithRestResponse("testContainer",
            "testBlob",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

        assertEquals(200, response.getStatusCode());
        assertEquals("testBody", response.getValue().string());
    }

    @Test
    public void downloadWithHeaders_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(200)
            .setBody("testBody");

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.downloadWithRestResponse("testContainer",
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
            new Callback<BlobDownloadAsyncResponse>() {
                @Override
                public void onResponse(BlobDownloadAsyncResponse response) {
                    try {
                        assertEquals(200, response.getStatusCode());
                        assertEquals("testBody", response.getValue().string());
                    } catch (IOException e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void stageBlock() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        BlockBlobsStageBlockResponse response = storageBlobClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null);

        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void stageBlock_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.stageBlock("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            new Callback<BlockBlobsStageBlockResponse>() {
                @Override
                public void onResponse(BlockBlobsStageBlockResponse response) {
                    assertEquals(201, response.getStatusCode());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void stageBlockWithRestResponse() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        BlockBlobsStageBlockResponse response = storageBlobClient.stageBlockWithRestResponse("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            null,
            null,
            null,
            null,
            null);

        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void stageBlockWithRestResponse_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201);

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.stageBlockWithRestResponse("testContainer",
            "testBlob",
            null,
            new byte[0],
            null,
            null,
            null,
            null,
            null,
            null,
            new Callback<BlockBlobsStageBlockResponse>() {
                @Override
                public void onResponse(BlockBlobsStageBlockResponse response) {
                    assertEquals(201, response.getStatusCode());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void commitBlockList() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        BlockBlobItem response = storageBlobClient.commitBlockList("testContainer",
            "testBlob",
            null,
            true);

        assertEquals(false, response.isServerEncrypted());
        assertEquals("testEtag", response.getETag());
    }

    @Test
    public void commitBlockList_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.commitBlockList("testContainer",
            "testBlob",
            null,
            true, new Callback<BlockBlobItem>() {
                @Override
                public void onResponse(BlockBlobItem response) {
                    assertEquals(false, response.isServerEncrypted());
                    assertEquals("testEtag", response.getETag());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
    }

    @Test
    public void commitBlockListWithRestResponse() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

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
            null);

        assertEquals(false, response.getBlockBlobItem().isServerEncrypted());
        assertEquals("testEtag", response.getBlockBlobItem().getETag());
    }

    @Test
    public void commitBlockListWithRestResponse_withCallback() {
        MockResponse mockResponse = new MockResponse()
            .setResponseCode(201)
            .setHeader("x-ms-request-server-encrypted", false)
            .setHeader("ETag", "testEtag")
            .setBody("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");

        mockWebServer.enqueue(mockResponse);

        ServiceCall serviceCall = storageBlobClient.commitBlockListWithRestResponse("testContainer",
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
            null, new Callback<BlockBlobsCommitBlockListResponse>() {
                @Override
                public void onResponse(BlockBlobsCommitBlockListResponse response) {
                    assertEquals(false, response.getBlockBlobItem().isServerEncrypted());
                    assertEquals("testEtag", response.getBlockBlobItem().getETag());
                }

                @Override
                public void onFailure(Throwable t) {
                    throw new RuntimeException(t);
                }
            });

        assertNotNull(serviceCall);
        assertFalse(serviceCall.isCanceled());
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
}
