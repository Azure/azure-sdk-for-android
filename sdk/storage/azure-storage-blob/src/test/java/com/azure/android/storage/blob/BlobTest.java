package com.azure.android.storage.blob;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersHeaders;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersResponse;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlobType;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.LeaseStateType;
import com.azure.android.storage.blob.models.LeaseStatusType;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.azure.android.storage.blob.TestUtils.awaitOnLatch;
import static com.azure.android.storage.blob.TestUtils.enableFiddler;
import static com.azure.android.storage.blob.TestUtils.garbageEtag;
import static com.azure.android.storage.blob.TestUtils.generateBlockID;
import static com.azure.android.storage.blob.TestUtils.generateResourceName;
import static com.azure.android.storage.blob.TestUtils.getDefaultData;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.TestUtils.initializeDefaultSyncBlobClientBuilder;
import static com.azure.android.storage.blob.TestUtils.newDate;
import static com.azure.android.storage.blob.TestUtils.oldDate;
import static com.azure.android.storage.blob.TestUtils.receivedEtag;
import static com.azure.android.storage.blob.TestUtils.setupMatchCondition;
import static com.azure.android.storage.blob.TestUtils.validateBasicHeaders;
import static com.azure.android.storage.blob.TestUtils.validateBlobProperties;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


@RunWith(DataProviderRunner.class)
public class BlobTest {
    private String containerName;
    private String blobName;
    private static StorageBlobAsyncClient asyncClient;
    private static StorageBlobClient syncClient;

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

    @DataProvider
    public static Object[][] headers() throws NoSuchAlgorithmException {
        return new Object[][] {
            {null, null, null, null, null, null}, // 0
            {"control", "disposition", "encoding", "language", MessageDigest.getInstance("MD5").digest(getDefaultData()), "type"},   // 1
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
    public void getPropertiesValues() {
        // When
        BlobGetPropertiesHeaders response = syncClient.getBlobProperties(containerName, blobName);

        // Then
        assertNotNull(response.getETag());
//        assertFalse(response.getETag().contains("\"")); // Quotes should be scrubbed from etag header values
        assertNotNull(response.getLastModified());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getVersion());
        assertNotNull(response.getDateProperty());
        assertTrue(response.getMetadata() == null || response.getMetadata().isEmpty());
        assertEquals(BlobType.BLOCK_BLOB, response.getBlobType());
        assertNull(response.getCopyCompletionTime()); // tested in "copy"
        assertNull(response.getCopyStatusDescription()); // only returned when the service has errors; cannot validate.
        assertNull(response.getCopyId()); // tested in "abort copy"
        assertNull(response.getCopyProgress()); // tested in "copy"
        assertNull(response.getCopySource()); // tested in "copy"
        assertNull(response.getCopyStatus()); // tested in "copy"
        assertTrue(response.isIncrementalCopy() == null || !response.isIncrementalCopy()); // tested in PageBlob."start incremental copy"
        assertNull(response.getDestinationSnapshot()); // tested in PageBlob."start incremental copy"
        assertNull(response.getLeaseDuration()); // tested in "acquire lease"
        assertEquals(LeaseStateType.AVAILABLE, response.getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, response.getLeaseStatus());
        assertTrue(response.getContentLength() >= 0);
        assertNotNull(response.getContentType());
        assertNull(response.getContentMD5());
        assertNull(response.getContentEncoding()); // tested in "set HTTP headers"
        assertNull(response.getContentDisposition()); // tested in "set HTTP headers"
        assertNull(response.getContentLanguage()); // tested in "set HTTP headers"
        assertNull(response.getCacheControl()); // tested in "set HTTP headers"
        assertNull(response.getBlobSequenceNumber()); // tested in PageBlob."create sequence number"
        assertEquals("bytes", response.getAcceptRanges());
        assertNull(response.getBlobCommittedBlockCount()); // tested in AppendBlob."append block"
        assertTrue(response.isServerEncrypted());
        assertEquals(AccessTier.HOT.toString(), response.getAccessTier());
        assertTrue(response.isAccessTierInferred());
        assertNull(response.getArchiveStatus());
        assertNotNull(response.getCreationTime());
        // Tag Count not in BlobProperties.
        // Rehydrate priority not in BlobProperties
        // Is Sealed not in BlobProperties
        // Last Access Time is not in BlobProperties
    }

    @Test
    public void getProperties() {
        // When
        BlobGetPropertiesResponse response = syncClient.getBlobPropertiesWithRestResponse(containerName, blobName,
            null, null, null, null, null, null, null);

        // Then
        assertEquals(200, response.getStatusCode());
    }

    // Get properties AC

    // Get properties AC fail

    @Test
    public void getPropertiesError() {
        // Setup
        String blobName = generateResourceName(); // Blob that does not exist.

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.getBlobProperties(containerName, blobName));

        // Then
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    public void setHttpHeadersMin() {
        // Setup
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("contentType");

        // When
        syncClient.setBlobHttpHeaders(containerName, blobName, headers);

        // Then
        BlobGetPropertiesHeaders responseHeaders = syncClient.getBlobProperties(containerName, blobName);
        assertEquals("contentType", responseHeaders.getMetadata().get("foo"));
    }

    @Test
    public void setHttpHeadersAllNull() {
        // When
        BlobSetHttpHeadersResponse response =
            syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, null, null, null, null);

        // Then
        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    @UseDataProvider("headers")
    public void setHttpHeadersHeaders(String cacheControl, String contentDisposition, String contentEncoding, String contentLanguage, byte[] contentMd5, String contentType) {
        // Setup
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMd5)
            .setContentType(contentType);

        // When
        BlobSetHttpHeadersResponse response =
            syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, null, headers, null, null);

        // Then
        validateBasicHeaders(response.getHeaders());
        assertEquals(200, response.getStatusCode());

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        BlobGetPropertiesHeaders getPropertiesHeaders = syncClient.getBlobProperties(containerName, blobName);
        validateBlobProperties(getPropertiesHeaders, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMd5, contentType);
    }

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void setHttpHeadersAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // Expect
        assertEquals(200, syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, requestConditions,
            null, null, null).getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void setHttpHeadersACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // Expect
        assertThrows(BlobStorageException.class,
            () -> syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, requestConditions, null,
                null, null));
    }

    @Test
    public void setHttpHeadersAsync() {
        // Setup
        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.setBlobHttpHeaders(containerName, blobName, null, null, null, null,
            null, null, new CallbackWithHeader<Void, BlobSetHttpHeadersHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetHttpHeadersHeaders header, Response response) {
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

        awaitOnLatch(latch, "setHttpHeaders");
    }

    @Test
    public void rawDownloadMin() throws IOException {
        // When
        ResponseBody response = syncClient.rawDownload(containerName, blobName);

        // Then
        assertArrayEquals(getDefaultData(), response.bytes());
    }

    @Test
    public void rawDownloadValues() throws IOException {
        // When
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertArrayEquals(getDefaultData(), response.getValue().bytes());
        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();
        assertTrue(headers.getMetadata() == null || headers.getMetadata().isEmpty());
        assertEquals(BlobType.BLOCK_BLOB, headers.getBlobType());
        assertNull(headers.getCopyCompletionTime()); // tested in "copy"
        assertNull(headers.getCopyStatusDescription()); // only returned when the service has errors; cannot validate.
        assertNull(headers.getCopyId()); // tested in "abort copy"
        assertNull(headers.getCopyProgress()); // tested in "copy"
        assertNull(headers.getCopySource()); // tested in "copy"
        assertNull(headers.getCopyStatus()); // tested in "copy"
        assertNull(headers.getLeaseDuration()); // tested in "acquire lease"
        assertEquals(LeaseStateType.AVAILABLE, headers.getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, headers.getLeaseStatus());
        assertTrue(headers.getContentLength() >= 0);
        assertNotNull(headers.getContentType());
        assertNull(headers.getContentMd5());
        assertNull(headers.getContentEncoding()); // tested in "set HTTP headers"
        assertNull(headers.getContentDisposition()); // tested in "set HTTP headers"
        assertNull(headers.getContentLanguage()); // tested in "set HTTP headers"
        assertNull(headers.getCacheControl()); // tested in "set HTTP headers"
        assertNull(headers.getBlobSequenceNumber()); // tested in PageBlob."create sequence number"
        assertEquals("bytes", headers.getAcceptRanges());
        assertNull(headers.getBlobCommittedBlockCount()); // tested in AppendBlob."append block"
        assertTrue(headers.isServerEncrypted());
    }

    @Test
    public void rawDownloadEmptyBlob() throws IOException {
        // Setup
        String blobName = generateResourceName(); // Blob that does not exist.
        syncClient.commitBlockList(containerName, blobName, new ArrayList<>(), false);

        // When
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertEquals("", response.getValue().string());
        assertEquals(200, response.getStatusCode());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();
        assertEquals(0, (long) headers.getContentLength());
    }

    // Download range
    // Download AC
    // Download AC fail
    // Download md5
    // Download snapshot  TODO (gapra) : Test this if we add support for snapshot creation?

    @Test
    public void rawDownloadError() {
        // Setup
        String blobName = generateResourceName(); // Blob that does not exist.

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.rawDownload(containerName, blobName));

        // Then
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    public void deleteMin() {
        // When
        syncClient.deleteBlob(containerName, blobName);

        // Then
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.getBlobProperties(containerName, blobName));
        assertEquals(404, ex.getStatusCode());
        assertEquals("BlobNotFound", ex.getErrorCode().toString());
    }

    @Test
    public void delete() {
        // When
        BlobDeleteResponse response = syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null, null, null, null, null, null);

        // Then
        assertEquals(202, response.getStatusCode());
        BlobDeleteHeaders headers = response.getDeserializedHeaders();
        assertNotNull(headers.getRequestId());
        assertNotNull(headers.getVersion());
        assertNotNull(headers.getDateProperty());
    }

    // Delete options TODO (gapra) : Test this if we add support for snapshot creation?

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobDeleteResponse response = syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null ,null,  null, requestConditions, null, null);

        // Then
        assertEquals(202, response.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null ,null,  null, requestConditions, null, null));

        // Then
        assertEquals(412, ex.getStatusCode());
    }

    @Test
    public void deleteError() {
        // Setup
        String blobName = generateResourceName(); // Blob that does not exist.

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.deleteBlob(containerName, blobName));

        // Then
        assertEquals(404, ex.getStatusCode());
    }
}
