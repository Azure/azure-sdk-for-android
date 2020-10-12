package com.azure.android.storage.blob;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import com.azure.android.storage.blob.TestUtils;

import static com.azure.android.storage.blob.TestUtils.enableFiddler;
import static com.azure.android.storage.blob.TestUtils.generateResourceName;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultSyncBlobClientBuilder;

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
        syncClient.de
    }
}
