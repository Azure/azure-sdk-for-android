package com.azure.android.storage.blob;

import com.azure.android.core.util.Base64Util;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobErrorCode;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.CpkInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.azure.android.storage.blob.TestUtils.enableFiddler;
import static com.azure.android.storage.blob.TestUtils.generateBlockID;
import static com.azure.android.storage.blob.TestUtils.generateResourceName;
import static com.azure.android.storage.blob.TestUtils.getDefaultData;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultSyncBlobClientBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BlockBlobTest {
    private String containerName;
    private String blobName;
    private static StorageBlobAsyncClient asyncClient;
    private static StorageBlobClient syncClient;

    @BeforeClass
    public static void setupClass() {
        asyncClient = initializeDefaultAsyncBlobClientBuilder(enableFiddler()).build();
        syncClient = initializeDefaultSyncBlobClientBuilder(enableFiddler()).build();
    }

    @Before
    public void setupTest() {
        // Create container
        containerName = generateResourceName();
        syncClient.createContainer(containerName);

        // Create blob
        blobName = generateResourceName();
        String blockId = generateBlockID();
        syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);
        syncClient.commitBlockList(containerName, blobName, blockIds, false);
    }

    @After
    public void teardownTest() {
        syncClient.deleteContainer(containerName);
    }

    @Test
    public void stageBlock() {
        // Setup
        String blockId = generateBlockID();

        // When
        BlockBlobsStageBlockResponse response = syncClient.stageBlockWithRestResponse(containerName, blobName, blockId, getDefaultData(), null, null, null ,null ,null ,null, null);

        // Then
        assertEquals(201, response.getStatusCode());
        BlockBlobStageBlockHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers.getXMsContentCrc64());
        assertNotNull(headers.getRequestId());
        assertNotNull(headers.getVersion());
        assertNotNull(headers.getDateProperty());
        assertTrue(headers.isServerEncrypted() != null && headers.isServerEncrypted());
    }

    @Test
    public void stageBlockMin() {
        // Setup
        String blockId = generateBlockID();

        // When
        Void response = syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);

        // Then
        // When list block support added, check blockBlobClient.listBlocks(BlockListType.ALL).getUncommittedBlocks().size() == 1
    }

    @Test
    public void stageBlockIABlockId() {
        // Setup
        String nullBlockId = null;

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.stageBlock(containerName, blobName, nullBlockId, getDefaultData(), null));

        // Then
        assertEquals(400, ex.getStatusCode());

        // Setup
        String wrongBlockId = "id";

        // When
        ex = assertThrows(BlobStorageException.class,
            () -> syncClient.stageBlock(containerName, blobName, wrongBlockId, getDefaultData(), null));

        // Then
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    public void stageBlockIAData() {
        // Setup
        byte[] nullData = null;

        // Expect
        assertThrows(NullPointerException.class,
            () -> syncClient.stageBlock(containerName, blobName, generateBlockID(), nullData, null));

        // Setup
        byte[] data = new byte[0];

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.stageBlock(containerName, blobName, generateBlockID(), data, null));

        // Then
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    public void stageBlockTransactionalMd5() throws NoSuchAlgorithmException {
        // Setup
        byte[] correctMd5 = MessageDigest.getInstance("MD5").digest(getDefaultData());

        // When
        BlockBlobsStageBlockResponse response = syncClient.stageBlockWithRestResponse(containerName, blobName, generateBlockID(), getDefaultData(), correctMd5, null, null, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());

        // Setup
        byte[] garbageMd5 = MessageDigest.getInstance("MD5").digest("garbage".getBytes());

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.stageBlock(containerName, blobName, generateBlockID(), getDefaultData(), garbageMd5));

        // Then
        assertEquals(400, ex.getStatusCode());
        assertEquals(BlobErrorCode.MD5MISMATCH, ex.getErrorCode());
    }

    @Test
    public void commitBlockList() {
        // Setup
        String blockId = generateBlockID();
        syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);

        // When
        BlockBlobsCommitBlockListResponse response = syncClient.commitBlockListWithRestResponse(containerName, blobName, blockIds, null ,null,  null, null, null, null, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());
        BlockBlobCommitBlockListHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers.getETag());
//        assertFalse(headers.getETag().contains("\"")); // Quotes should be scrubbed from etag header values
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getRequestId());
        assertNotNull(headers.getVersion());
        assertNotNull(headers.getDateProperty());
        assertNotNull(headers.getXMsContentCrc64());
        assertTrue(headers.isServerEncrypted() != null && headers.isServerEncrypted());
    }

    @Test
    public void commitBlockListMinOverwrite() {
        // Setup
        String blockId = generateBlockID();
        syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);

        // When
        BlockBlobItem response = syncClient.commitBlockList(containerName, blobName, blockIds, true);

        // then
        assertNotNull(response);
    }

    @Test
    public void commitBlockListMinNoOverwrite() {
        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.commitBlockList(containerName, blobName, new ArrayList<>(), false));

        // then
        assertEquals(409, ex.getStatusCode());
        assertEquals(BlobErrorCode.BLOB_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    public void commitBlockListNull() {
        // When
        BlockBlobItem response = syncClient.commitBlockList(containerName, blobName, null, true);

        // then
        assertNotNull(response);
    }

    @Test
    public void commitBlockListHeaders() {
        // When
        BlockBlobItem response = syncClient.commitBlockList(containerName, blobName, null, true);

        // then
        assertNotNull(response);
    }

}
