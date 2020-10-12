package com.azure.android.storage.blob;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.TestUtils;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.ContainerCreateHeaders;
import com.azure.android.storage.blob.models.ContainerCreateResponse;

import okhttp3.Response;

import static com.azure.android.storage.blob.TestUtils.enableFiddler;
import static com.azure.android.storage.blob.TestUtils.generateResourceName;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultSyncBlobClientBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class ContainerTest {
    private String containerName;
    private static StorageBlobAsyncClient asyncClient;
    private static StorageBlobClient syncClient;


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
        ContainerCreateResponse response = syncClient.createContainerWithRestResponse(containerName, null,
            null, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void createAsync() {
        // Setup
        String containerName = generateResourceName();

        // When
        asyncClient.createContainer(containerName, null, null, null, null,
            null, null, new CallbackWithHeader<Void, ContainerCreateHeaders>() {
                @Override
                public void onSuccess(Void result, ContainerCreateHeaders header, Response response) {
                    assertEquals(201, response.code());
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    fail("Failed with exception: " + throwable.getMessage());
                }
            });
    }

    // create public Access

    // create metadata

    // Create error tested in min because it throws an expected exception.

    // Cancellation token? Request Id? Version?

    @Test
    public void deleteMin() throws InterruptedException {
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
        syncClient.createContainer()
    }
}
