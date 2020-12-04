// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.ContainerCreateHeaders;
import com.azure.android.storage.blob.models.ContainerCreateResponse;
import com.azure.android.storage.blob.models.ContainerDeleteHeaders;
import com.azure.android.storage.blob.models.ContainerDeleteResponse;
import com.azure.android.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.android.storage.blob.models.ContainerGetPropertiesResponse;
import com.azure.android.storage.blob.models.PublicAccessType;
import com.azure.android.storage.blob.options.ContainerCreateOptions;
import com.azure.android.storage.blob.options.ContainerDeleteOptions;
import com.azure.android.storage.blob.options.ContainerGetPropertiesOptions;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.OffsetDateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Response;

import static com.azure.android.storage.blob.common.TestUtils.awaitOnLatch;
import static com.azure.android.storage.blob.BlobTestUtils.enableFiddler;
import static com.azure.android.storage.blob.BlobTestUtils.generateResourceName;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultSyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.newDate;
import static com.azure.android.storage.blob.BlobTestUtils.oldDate;
import static com.azure.android.storage.blob.BlobTestUtils.validateBasicHeaders;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

@RunWith(DataProviderRunner.class)
@Ignore
public class ContainerTest {
    private String containerName;
    private static StorageBlobAsyncClient asyncClient;
    private static StorageBlobClient syncClient;

    @DataProvider
    public static Object[][] accessConditionsSuccess() {
        return new Object[][] {
            {null,    null},   // 0
            {oldDate, null},   // 1
            {null,    newDate} // 2
        };
    }

    @DataProvider
    public static Object[][] accessConditionsFail() {
        return new Object[][] {
            {newDate, null},   // 0
            {null,    oldDate} // 1
        };
    }

    @DataProvider
    public static Object[][] deleteAccessConditionsIllegal() {
        return new Object[][] {
            {"garbage", null,      null},     // 0
            {null,      "garbage", null},     // 1
            {null,      null,      "garbage"} // 2
        };
    }

    @DataProvider
    public static Object[][] getPropertiesAccessConditionsIllegal() {
        return new Object[][] {
            {"garbage", null,      null,    null,    null},     // 0
            {null,      "garbage", null,    null,    null},     // 1
            {null,      null,      oldDate, null,    null},     // 2
            {null,      null,      null,    oldDate, null},     // 3
            {null,      null,      null,    null,    "garbage"} // 4
        };
    }

    @BeforeClass
    public static void setupClass() {
        asyncClient = initializeDefaultAsyncBlobClientBuilder(enableFiddler()).build();
        syncClient = initializeDefaultSyncBlobClientBuilder(enableFiddler()).build();
    }

    @Before
    public void setupTest() {
        containerName = generateResourceName();
        syncClient.createContainer(containerName);
    }

    @After
    public void teardownTest() {
        syncClient.deleteContainer(containerName);
    }

    @Test
    public void createMin() {
        // Setup
        String containerName = generateResourceName();

        // When
        syncClient.createContainer(containerName);

        // Then. Creating again should throw.
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.createContainer(containerName));
        assertEquals(409, ex.getStatusCode());
    }

    @Test
    public void createAllNull() {
        // Setup
        String containerName = generateResourceName();

        // When
        ContainerCreateResponse response = syncClient.createContainerWithResponse(new ContainerCreateOptions(containerName));

        // Then
        assertEquals(201, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void createPublicAccess() {
        // Setup
        String containerName = generateResourceName();

        // When
        ContainerCreateResponse response = syncClient.createContainerWithResponse(new ContainerCreateOptions(containerName)
            .setPublicAccessType(PublicAccessType.BLOB));

        // Then
        assertEquals(PublicAccessType.BLOB, syncClient.getContainerProperties(containerName).getBlobPublicAccess());
    }

    @Test
    public void createMetadata() {
        // Setup
        String containerName = generateResourceName();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // When
        syncClient.createContainerWithResponse(new ContainerCreateOptions(containerName).setMetadata(metadata));

        // Then
        ContainerGetPropertiesHeaders headers = syncClient.getContainerProperties(containerName);
        assertEquals("value1", headers.getMetadata().get("key1"));
        assertEquals("value2", headers.getMetadata().get("key2"));
    } // TODO should we test special characters or that weird bug that affects ordering for string to sign?

    @Test
    public void createAsync() {
        // Setup
        String containerName = generateResourceName();

        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.createContainer(new ContainerCreateOptions(containerName),
            new CallbackWithHeader<Void, ContainerCreateHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerCreateHeaders header, Response response) {
                    assertEquals(201, response.code());
                    latch.countDown();
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

    // Create error tested in min because it throws an expected exception.

    // Cancellation token? Request Id? Version?

    @Test
    public void deleteMin() {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);

        // When
        syncClient.deleteContainer(containerName);

        // Then. Creating again without waiting 30s will result in a conflict.
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.createContainer(containerName));
        assertEquals(409, ex.getStatusCode());
        assertEquals("ContainerBeingDeleted", ex.getErrorCode().toString());
    }

    @Test
    public void deleteAllNull() {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);

        // When
        ContainerDeleteResponse response = syncClient.deleteContainerWithResponse(new ContainerDeleteOptions(containerName));

        // Then
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.getContainerProperties(containerName));
        assertEquals(404, ex.getStatusCode());
        assertNotNull(response.getDeserializedHeaders().getRequestId());
        assertNotNull(response.getDeserializedHeaders().getVersion());
        assertNotNull(response.getDeserializedHeaders().getDateProperty());
    }

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified) {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        // When
        syncClient.deleteContainerWithResponse(new ContainerDeleteOptions(containerName).setRequestConditions(requestConditions));

        // Then
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.getContainerProperties(containerName));
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified) {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.deleteContainerWithResponse(new ContainerDeleteOptions(containerName).setRequestConditions(requestConditions)));

        // Then
        assertEquals(412, ex.getStatusCode());
    }

    @Test
    @UseDataProvider("deleteAccessConditionsIllegal")
    public void deleteACIllegal(String ifMatch, String ifNoneMatch, String tagsConditions) {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch)
            .setTagsConditions(tagsConditions);

        // Expect
        assertThrows(UnsupportedOperationException.class,
            () -> syncClient.deleteContainerWithResponse(new ContainerDeleteOptions(containerName).setRequestConditions(requestConditions)));
    }

    @Test
    public void deleteAsync() {
        // Setup
        String containerName = generateResourceName();
        syncClient.createContainer(containerName);

        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.deleteContainer(new ContainerDeleteOptions(containerName),
            new CallbackWithHeader<Void, ContainerDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerDeleteHeaders header, Response response) {
                    assertEquals(202, response.code());
                    latch.countDown();
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

    public void deleteError() {
        // Setup
        String containerName = generateResourceName();

        // Expect
        assertThrows(BlobStorageException.class,
            () -> syncClient.deleteContainer(containerName));
    }

    @Test
    public void getPropertiesMin() {
        // Expect
        assertNotNull(syncClient.getContainerProperties(containerName));
    }

    @Test
    public void getPropertiesAllNull() {
        // When
        ContainerGetPropertiesResponse response = syncClient.getContainerPropertiesWithResponse(new ContainerGetPropertiesOptions(containerName));
        ContainerGetPropertiesHeaders headers = response.getDeserializedHeaders();

        // Then
        validateBasicHeaders(response.getHeaders());
        assertNull(headers.getBlobPublicAccess());
        assertFalse(headers.hasImmutabilityPolicy());
        assertFalse(headers.hasLegalHold());
        assertEquals(0, headers.getMetadata().size());
    }

    @Test
    @UseDataProvider("getPropertiesAccessConditionsIllegal")
    public void getPropertiesACIllegal(String ifMatch, String ifNoneMatch, OffsetDateTime modified, OffsetDateTime unmodified, String tagsConditions) {
        // Setup
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tagsConditions);

        // Expect
        assertThrows(UnsupportedOperationException.class,
            () -> syncClient.getContainerPropertiesWithResponse(new ContainerGetPropertiesOptions(containerName).setRequestConditions(requestConditions)));
    }

    @Test
    public void getPropertiesAsync() {
        // Setup
        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.getContainerProperties(new ContainerGetPropertiesOptions(containerName),
            new CallbackWithHeader<Void, ContainerGetPropertiesHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerGetPropertiesHeaders header, Response response) {
                    assertEquals(200, response.code());
                    latch.countDown();
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

        awaitOnLatch(latch, "getContainerProperties");
    }

    @Test
    public void getPropertiesError() {
        // Setup
        String containerName = generateResourceName();

        // Expect
        assertThrows(BlobStorageException.class,
            () -> syncClient.getContainerProperties(containerName));
    }
}
