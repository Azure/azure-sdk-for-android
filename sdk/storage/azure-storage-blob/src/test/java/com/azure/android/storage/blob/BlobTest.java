package com.azure.android.storage.blob;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.ArchiveStatus;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItemProperties;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersHeaders;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersResponse;
import com.azure.android.storage.blob.models.BlobSetMetadataHeaders;
import com.azure.android.storage.blob.models.BlobSetMetadataResponse;
import com.azure.android.storage.blob.models.BlobSetTagsHeaders;
import com.azure.android.storage.blob.models.BlobSetTagsResponse;
import com.azure.android.storage.blob.models.BlobSetTierResponse;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlobType;
import com.azure.android.storage.blob.models.LeaseStateType;
import com.azure.android.storage.blob.models.LeaseStatusType;
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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.azure.android.core.common.TestUtils.awaitOnLatch;
import static com.azure.android.storage.blob.BlobTestUtils.enableFiddler;
import static com.azure.android.storage.blob.BlobTestUtils.garbageEtag;
import static com.azure.android.storage.blob.BlobTestUtils.generateBlockID;
import static com.azure.android.storage.blob.BlobTestUtils.generateResourceName;
import static com.azure.android.storage.blob.BlobTestUtils.getDefaultData;
import static com.azure.android.storage.blob.BlobTestUtils.getDefaultString;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultAsyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.initializeDefaultSyncBlobClientBuilder;
import static com.azure.android.storage.blob.BlobTestUtils.newDate;
import static com.azure.android.storage.blob.BlobTestUtils.oldDate;
import static com.azure.android.storage.blob.BlobTestUtils.receivedEtag;
import static com.azure.android.storage.blob.BlobTestUtils.setupMatchCondition;
import static com.azure.android.storage.blob.BlobTestUtils.validateBasicHeaders;
import static com.azure.android.storage.blob.BlobTestUtils.validateBlobProperties;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


@RunWith(DataProviderRunner.class)
@Ignore
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
    public static Object[][] downloadRange() {
        return new Object[][] {
            {0L, null, getDefaultString()},                       // 0
            {0L, 5L,   getDefaultString().substring(0, 0 + 5)},   // 1
            {3L, 2L,   getDefaultString().substring(3, 3 + 2)},   // 2
            {1L, 3L,   getDefaultString().substring(1, 1 + 3)}    // 3
        };
    }

    @DataProvider
    public static Object[][] headers() throws NoSuchAlgorithmException {
        return new Object[][] {
            {null, null, null, null, null, null}, // 0
            {"control", "disposition", "encoding", "language", MessageDigest.getInstance("MD5").digest(getDefaultData()), "type"},   // 1
        };
    }

    @DataProvider
    public static Object[][] metadata() {
        return new Object[][]{
            {null, null, null, null},       // 0
            {"foo", "bar", "fizz", "buzz"}, // 1
            {"i0", "a", "i_", "a"}          // 2. Test culture sensitive word sort.
        };
    }

    @DataProvider
    public static Object[][] tierBlockBlob() {
        return new Object[][] {
            {AccessTier.HOT},       // 0
            {AccessTier.COOL},      // 1
            {AccessTier.ARCHIVE},   // 2
        };
    }

    @DataProvider
    public static Object[][] tierArchiveStatus() {
        return new Object[][] {
            {AccessTier.ARCHIVE, AccessTier.COOL, ArchiveStatus.REHYDRATE_PENDING_TO_COOL},    // 0
            {AccessTier.ARCHIVE, AccessTier.HOT,  ArchiveStatus.REHYDRATE_PENDING_TO_HOT},     // 1
        };
    }

    @DataProvider
    public static Object[][] tags() {
        return new Object[][] {
            {null, null, null, null},
            {"foo", "bar", "fizz", "buzz"},
            {" +-./:=_  +-./:=_", " +-./:=_", null, null}
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
            null, null, null, null, null);

        // Then
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobGetPropertiesResponse response = syncClient.getBlobPropertiesWithRestResponse(containerName, blobName, null, null, requestConditions, null,null);

        // Then
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void getPropertiesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.getBlobPropertiesWithRestResponse(containerName, blobName, null, null, requestConditions, null, null));

        // Then
        assertTrue(ex.getStatusCode() == 304 || ex.getStatusCode() == 412);
    }

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
        assertEquals("contentType", responseHeaders.getContentType());
    }

    @Test
    public void setHttpHeadersAllNull() {
        // When
        BlobSetHttpHeadersResponse response =
            syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, null, null);

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
            syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, null, headers, null);

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
        assertEquals(200, syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, requestConditions,
            null, null).getStatusCode());
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
            () -> syncClient.setBlobHttpHeadersWithResponse(containerName, blobName, null, requestConditions, null,
                null));
    }

    @Test
    public void setHttpHeadersAsync() {
        // Setup
        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.setBlobHttpHeaders(containerName, blobName, null, null,
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
    public void setMetadataMin() {
        // Setup
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");

        // When
        syncClient.setBlobMetadata(containerName, blobName, metadata);

        // Then
        BlobGetPropertiesHeaders headers = syncClient.getBlobProperties(containerName, blobName);
        assertEquals(1, headers.getMetadata().size());
        assertEquals("bar", headers.getMetadata().get("foo"));
    }

    @Test
    public void setMetadataAllNull() {
        // When
        BlobSetMetadataResponse response =
            syncClient.setBlobMetadataWithResponse(containerName, blobName, null, null, null, null, null);

        // Then
        assertEquals(0, syncClient.getBlobProperties(containerName, blobName).getMetadata().size());
        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        assertTrue(response.getDeserializedHeaders().isServerEncrypted());
    }

    @Test
    @UseDataProvider("metadata")
    public void setMetadataMetadata(String key1, String value1, String key2, String value2) {
        // Setup
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        // When
        BlobSetMetadataResponse response =
            syncClient.setBlobMetadataWithResponse(containerName, blobName, null, null, metadata, null,
                null);

        // Then
        validateBasicHeaders(response.getHeaders());
        BlobGetPropertiesHeaders headers = syncClient.getBlobProperties(containerName, blobName);
        assertEquals(metadata.size(), headers.getMetadata().size());
        for(Map.Entry<String, String> entry : metadata.entrySet()) {
            assertEquals(entry.getValue(), headers.getMetadata().get(entry.getKey()));
        }
    }

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void setMetadataAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // Expect
        assertEquals(200, syncClient.setBlobMetadataWithResponse(containerName, blobName, null, requestConditions,
            null, null, null).getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void setMetadataACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // Expect
        assertThrows(BlobStorageException.class,
            () -> syncClient.setBlobMetadataWithResponse(containerName, blobName, null, requestConditions, null,
                null, null));
    }

    @Test
    public void setMetadataAsync() {
        // Setup
        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.setBlobMetadata(containerName, blobName, null, null, null,
            null, null, new CallbackWithHeader<Void, BlobSetMetadataHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetMetadataHeaders header, Response response) {
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

        awaitOnLatch(latch, "setBlobMetadata");
    }

    // setMetadataError tested in AC fail as it throws BlobStorageException

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
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, null, null, null, null, null);

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
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, null, null, null, null, null);

        // Then
        assertEquals("", response.getValue().string());
        assertEquals(200, response.getStatusCode());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();
        assertEquals(0, (long) headers.getContentLength());
    }

    @Test
    @UseDataProvider("downloadRange")
    public void rawDownloadRange(Long offset, Long count, String expectedData) throws IOException {
        // Setup
        BlobRange range = count == null ? new BlobRange(offset) : new BlobRange(offset, count);

        // When
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, range, null, null,  null, null, null);

        // Then
        assertEquals(expectedData, response.getValue().string());
        assertTrue(count == null ? response.getStatusCode() == 200 : response.getStatusCode() == 206);
    }

    @Test
    @UseDataProvider("accessConditionsSuccess")
    public void rawDownloadAC(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifMatch = setupMatchCondition(syncClient, containerName, blobName, ifMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, requestConditions, null, null, null, null);

        // Then
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @UseDataProvider("accessConditionsFail")
    public void rawDownloadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String ifMatch, String ifNoneMatch) {
        // Setup
        ifNoneMatch = setupMatchCondition(syncClient, containerName, blobName, ifNoneMatch);
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch);

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, null, requestConditions, null, null, null, null));

        // Then
        assertTrue(ex.getStatusCode() == 304 || ex.getStatusCode() == 412);
    }

    @Test
    public void rawDownloadMd5() throws IOException, NoSuchAlgorithmException {
        // When
        BlobDownloadResponse response = syncClient.rawDownloadWithRestResponse(containerName, blobName, null, null, new BlobRange(0L, 3L), null, true, null, null, null);

        // Then
        assertEquals(getDefaultString().substring(0, 3), response.getValue().string());
        assertEquals(206, response.getStatusCode());
        BlobDownloadHeaders headers = response.getDeserializedHeaders();
        assertArrayEquals(MessageDigest.getInstance("MD5").digest(getDefaultString().substring(0, 3).getBytes()), headers.getContentMd5());
    }

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
        BlobDeleteResponse response = syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null, null, null, null);

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
        BlobDeleteResponse response = syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null ,null, requestConditions, null);

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
            () -> syncClient.deleteBlobWithRestResponse(containerName, blobName, null, null ,null, requestConditions, null));

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

    @Test
    @UseDataProvider("tierBlockBlob")
    public void setBlobTierBlockBlob(AccessTier tier) {
        // When
        BlobSetTierResponse response = syncClient.setBlobTierWithRestResponse(containerName, blobName, tier, null, null, null, null, null);

        // Then
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 202);

        BlobGetPropertiesHeaders getProperties = syncClient.getBlobProperties(containerName, blobName);
        assertEquals(tier.toString(), getProperties.getAccessTier());

        BlobItemProperties listProperties = syncClient.getBlobsInPage(null, containerName, null).getItems().get(0).getProperties();
        assertEquals(tier, listProperties.getAccessTier());
    }

    // TODO (gapra) : set access tier page blob if page blob support added. Should we expose page blob tiers if not supported? I think no

    @Test
    public void setBlobTierMin() {
        // When
        Void response = syncClient.setBlobTier(containerName, blobName, AccessTier.HOT);

        // Then
        BlobGetPropertiesHeaders getProperties = syncClient.getBlobProperties(containerName, blobName);
        assertEquals(AccessTier.HOT.toString(), getProperties.getAccessTier());
    }

    @Test
    public void setBlobTierInferred() {
        // Setup
        Boolean getPropertiesInferredBefore = syncClient.getBlobProperties(containerName, blobName).isAccessTierInferred();
        Boolean listBlobsInferredBefore = syncClient.getBlobsInPage(null, containerName, null).getItems().get(0).getProperties().isAccessTierInferred();

        // When
        syncClient.setBlobTier(containerName, blobName, AccessTier.HOT);
        Boolean getPropertiesInferredAfter = syncClient.getBlobProperties(containerName, blobName).isAccessTierInferred();
        Boolean listBlobsInferredAfter = syncClient.getBlobsInPage(null, containerName, null).getItems().get(0).getProperties().isAccessTierInferred();

        // Then
        assertTrue(getPropertiesInferredBefore);
        assertTrue(listBlobsInferredBefore);

        assertNotEquals(Boolean.TRUE, getPropertiesInferredAfter);
        assertNotEquals(Boolean.TRUE, listBlobsInferredAfter);
    }

    @Test
    @UseDataProvider("tierArchiveStatus")
    public void setBlobTierArchiveStatus(AccessTier sourceTier, AccessTier destTier, ArchiveStatus status) {
        // When
        syncClient.setBlobTier(containerName, blobName, sourceTier);
        syncClient.setBlobTier(containerName, blobName, destTier);

        // Then
        BlobGetPropertiesHeaders getProperties = syncClient.getBlobProperties(containerName, blobName);
        assertEquals(status.toString(), getProperties.getArchiveStatus());

        BlobItemProperties listProperties = syncClient.getBlobsInPage(null, containerName, null).getItems().get(0).getProperties();
        assertEquals(status, listProperties.getArchiveStatus());
    }

    // Set tier snapshot

    // Set tier snapshot error

    @Test
    public void setBlobTierIA() {
        // Expect
        NullPointerException ex = assertThrows(NullPointerException.class,
            () -> syncClient.setBlobTier(containerName, blobName, null));
    }

    // Set tier lease

    // Set tier lease error

    // Set tier tags

    // Set tier tags fail

    @Test
    public void setBlobTierError() {
        // Setup
        String blobName = generateResourceName(); // Blob that does not exist.

        // When
        BlobStorageException ex = assertThrows(BlobStorageException.class,
            () -> syncClient.setBlobTier(containerName, blobName, AccessTier.HOT));

        // Then
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    public void setTagsMin() {
        // Setup
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        // When
        syncClient.setBlobTags(containerName, blobName, tags);

        // Then
        Map<String, String> responseTags = syncClient.getBlobTags(containerName, blobName);
        assertEquals(1, responseTags.size());
        assertEquals("bar", responseTags.get("foo"));
    }

    @Test
    public void setTagsAllNull() {
        // When
        BlobSetTagsResponse response =
            syncClient.setBlobTagsWithResponse(containerName, blobName, null, null, null, null);

        // Then
        assertEquals(0, syncClient.getBlobProperties(containerName, blobName).getMetadata().size());
        assertEquals(204, response.getStatusCode());
        assertNotNull(response.getHeaders().get("x-ms-request-id"));
        assertNotNull(response.getHeaders().get("x-ms-version"));
        assertNotNull(response.getHeaders().get("date"));
    }

    @Test
    @UseDataProvider("tags")
    public void setTagsTags(String key1, String value1, String key2, String value2) {
        // Setup
        Map<String, String> tags = new HashMap<>();
        if (key1 != null && value1 != null) {
            tags.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            tags.put(key2, value2);
        }

        // When
        BlobSetTagsResponse response =
            syncClient.setBlobTagsWithResponse(containerName, blobName, null, null,  tags, null);

        // Then
        Map<String, String> tagsResponse = syncClient.getBlobTags(containerName, blobName);
        assertEquals(tags.size(), tagsResponse.size());
        for(Map.Entry<String, String> entry : tags.entrySet()) {
            assertEquals(entry.getValue(), tagsResponse.get(entry.getKey()));
        }
    }

    @Test
    public void setTagsAC() {
        // Setup
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        syncClient.setBlobTags(containerName, blobName, t);
        t = new HashMap<>();
        t.put("fizz", "buzz");

        // Expect
        assertEquals(204, syncClient.setBlobTagsWithResponse(containerName, blobName, null, "\"foo\" = 'bar'",
            t, null).getStatusCode());
    }

    @Test
    public void setTagsACFail() {
        // Setup
        Map<String, String> t = new HashMap<>();
        t.put("fizz", "buzz");

        // Expect
        assertThrows(BlobStorageException.class,
            () -> syncClient.setBlobTagsWithResponse(containerName, blobName, null, "\"foo\" = 'bar'", t,
                null));
    }

    @Test
    public void setTagsAsync() {
        // Setup
        CountDownLatch latch = new CountDownLatch(1);

        // When
        asyncClient.setBlobTags(containerName, blobName, null, null, null,
            null, new CallbackWithHeader<Void, BlobSetTagsHeaders>() {
                @Override
                public void onSuccess(Void result, BlobSetTagsHeaders header, Response response) {
                    assertEquals(204, response.code());
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

        awaitOnLatch(latch, "setBlobTags");
    }

    // setTagsError tested in AC fail as it throws BlobStorageException
}
