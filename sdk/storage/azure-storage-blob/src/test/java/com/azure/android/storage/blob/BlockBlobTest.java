package com.azure.android.storage.blob;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.models.BlobErrorCode;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.azure.android.core.common.TestUtils.awaitOnLatch;
import static com.azure.android.storage.blob.BlobTestUtils.enableFiddler;
import static com.azure.android.storage.blob.BlobTestUtils.garbageEtag;
import static com.azure.android.storage.blob.BlobTestUtils.generateBlockID;
import static com.azure.android.storage.blob.BlobTestUtils.generateResourceName;
import static com.azure.android.storage.blob.BlobTestUtils.getDefaultData;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultSyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.newDate;
import static com.azure.android.storage.blob.BlobTestUtils.oldDate;
import static com.azure.android.storage.blob.BlobTestUtils.receivedEtag;
import static com.azure.android.storage.blob.BlobTestUtils.setupMatchCondition;
import static com.azure.android.storage.blob.BlobTestUtils.validateBasicHeaders;
import static com.azure.android.storage.blob.BlobTestUtils.validateBlobProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
@Ignore
public class BlockBlobTest {
    private String containerName;
    private String blobName;
    private static StorageBlobAsyncClient asyncClient;
    private static StorageBlobClient syncClient;

    @DataProvider
    public static Object[][] headers() throws NoSuchAlgorithmException {
        return new Object[][] {
            {null,      null,          null,       null,       null,                                                       null},    // 0
            {"control", "disposition", "encoding", "language", MessageDigest.getInstance("MD5").digest(getDefaultData()), "type"},   // 1
        };
    }

    // TODO: (gapra) Add iftags
    @DataProvider
    public static Object[][] accessConditionsSuccess() {
        return new Object[][] {
            {null,    null,    null,         null},       // 0
            {oldDate, null,    null,         null},       // 1
            {null,    newDate, null,         null},       // 2
            {null,    null,    receivedEtag, null},       // 3
            {null,    null,    null,         garbageEtag} // 4
        };
    }

    // TODO: (gapra) Add iftags
    @DataProvider
    public static Object[][] accessConditionsFail() {
        return new Object[][] {
            {newDate, null,    null,        null},        // 0
            {null,    oldDate, null,        null},        // 1
            {null,    null,    garbageEtag, null},        // 2
            {null,    null,    null,        receivedEtag} // 3
        };
    }

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
        BlockBlobsStageBlockResponse response = syncClient.stageBlockWithRestResponse(containerName, blobName, blockId, getDefaultData(), null, null, null, null ,null, null, null);

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
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);

        // When
        Void response = syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);

        // Then
        // This would throw if the above stage block failed.
        syncClient.commitBlockList(containerName, blobName, blockIds, true);
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
    public void stageBlockComputeMd5() {
        // Success Case
        // When
        BlockBlobsStageBlockResponse response = syncClient.stageBlockWithRestResponse(containerName, blobName, generateBlockID(), getDefaultData(), null, null, true, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());
    }

    @Test
    public void stageBlockComputeMd5Error() throws NoSuchAlgorithmException {
        // Error Case - data was modified after md5 was computed
        // When
        StorageBlobClient tempClient = initializeDefaultSyncBlobClientBuilder(enableFiddler(), chain -> {
            Request newRequest = chain.request()
                .newBuilder()
                .put(RequestBody.create(null, "Jr;;p Ept;f".getBytes(StandardCharsets.UTF_8)))
                .build();
            return chain.proceed(newRequest);
        }).build();
        BlobStorageException exception = assertThrows(BlobStorageException.class,
            () -> tempClient.stageBlockWithRestResponse(containerName, blobName, generateBlockID(), getDefaultData(), null, null, true, null, null, null, null));

        // Then
        assertEquals(BlobErrorCode.MD5MISMATCH, exception.getErrorCode());

        // Error Case - computeMd5 conflicts with contentMd5 being passed in
        // Setup
        byte[] correctMd5 = MessageDigest.getInstance("MD5").digest(getDefaultData());

        // When
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> syncClient.stageBlockWithRestResponse(containerName, blobName, generateBlockID(), getDefaultData(), correctMd5, null, true, null, null, null, null));

        // Then
        assertEquals("'transactionalContentMD5' can not be set when 'computeMd5' is true.", ex.getMessage());
    }

    // No Stage Block Lease tests due to no support for leases yet.

    // Stage block error tested in tests above.

    @Test
    public void stageBlockAsync() {
        // Setup
        String blockId = generateBlockID();

        CountDownLatch latch = new CountDownLatch(1);

        // Expect
        asyncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null, null, false, null,
            null, null, null, new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobStageBlockHeaders headers, Response response) {
                    assertEquals(201, response.code());
                    assertNotNull(headers.getXMsContentCrc64()); // TODO (gapra) : Get rid of this publically by handwriting public types
                    assertNotNull(headers.getRequestId());
                    assertNotNull(headers.getVersion());
                    assertNotNull(headers.getDateProperty());
                    assertTrue(headers.isServerEncrypted() != null && headers.isServerEncrypted());
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

        awaitOnLatch(latch, "stageBlock");
    }

    @Test
    public void commitBlockList() {
        // Setup
        String blockId = generateBlockID();
        syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);

        // When
        BlockBlobsCommitBlockListResponse response = syncClient.commitBlockListWithRestResponse(containerName, blobName, blockIds, null ,null, null, null, null, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        BlockBlobCommitBlockListHeaders headers = response.getDeserializedHeaders();
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
    @UseDataProvider("headers")
    public void commitBlockListHeaders(String cacheControl, String contentDisposition, String contentEncoding, String contentLanguage, byte[] contentMd5, String contentType) {
        // Setup
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMd5)
            .setContentType(contentType);

        // When
        BlockBlobsCommitBlockListResponse response = syncClient.commitBlockListWithRestResponse(containerName, blobName, null, null ,null,  null, headers, null, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        BlobGetPropertiesHeaders getPropertiesHeaders = syncClient.getBlobProperties(containerName, blobName);
        validateBlobProperties(getPropertiesHeaders, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMd5, contentType);
    }


    @Test
    public void commitBlockListMetadata() {
        // Setup
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // When
        BlockBlobsCommitBlockListResponse response = syncClient.commitBlockListWithRestResponse(containerName, blobName, null, null ,null,  null, null, metadata, null, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());

        BlobGetPropertiesHeaders headers = syncClient.getBlobProperties(containerName, blobName);
        assertEquals("value1", headers.getMetadata().get("key1"));
        assertEquals("value2", headers.getMetadata().get("key2"));
    }

    // TODO: (gapra) Commit block list tags

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void commitBlockListAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlockBlobsCommitBlockListResponse response = syncClient.commitBlockListWithRestResponse(containerName, blobName, null, null ,null,  null, null, null, requestConditions, null, null, null);

        // Then
        assertEquals(201, response.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void commitBlockListACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.commitBlockListWithRestResponse(containerName, blobName, null, null ,null,  null, null, null, requestConditions, null, null, null));

        // Then
        assertEquals(412, ex.getStatusCode());
    }

    // Commit block list error tested in tests above.

    @Test
    public void commitBlockListAsync() {
        // Setup
        String blockId = generateBlockID();
        syncClient.stageBlock(containerName, blobName, blockId, getDefaultData(), null);
        List<String> blockIds = new ArrayList<>();
        blockIds.add(blockId);

        CountDownLatch latch = new CountDownLatch(1);

        // Expect
        asyncClient.commitBlockList(containerName, blobName, blockIds, null, null, null, null,
            null, null, null, null, null, new CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(BlockBlobItem result, BlockBlobCommitBlockListHeaders headers, Response response) {
                    assertEquals(201, response.code());
                    validateBasicHeaders(response.headers());
                    assertNotNull(headers.getXMsContentCrc64());
                    assertTrue(headers.isServerEncrypted() != null && headers.isServerEncrypted());
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

        awaitOnLatch(latch, "commitBlockList");

    }
}
