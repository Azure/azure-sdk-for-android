// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import androidx.annotation.NonNull;

import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.CancellationTokenImpl;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.Base64Util;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.storage.blob.interceptor.MetadataInterceptor;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.BlockLookupList;
import com.azure.android.storage.blob.models.ContainerCreateHeaders;
import com.azure.android.storage.blob.models.ContainerCreateResponse;
import com.azure.android.storage.blob.models.ContainerDeleteHeaders;
import com.azure.android.storage.blob.models.ContainerDeleteResponse;
import com.azure.android.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.android.storage.blob.models.ContainerGetPropertiesResponse;
import com.azure.android.storage.blob.models.ListBlobFlatSegmentHeaders;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.android.storage.blob.models.EncryptionAlgorithmType;
import com.azure.android.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.models.PublicAccessType;

import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Tag;

/**
 * PACKAGE PRIVATE CLASS AND METHODS
 */
final class StorageBlobServiceImpl {
    private final StorageBlobService service;
    private final SerializerAdapter serializerAdapter;
    private static String XMS_VERSION = "2019-02-02";

    StorageBlobServiceImpl(ServiceClient serviceClient) {
        this.service = serviceClient.getRetrofit().create(StorageBlobService.class);
        this.serializerAdapter = SerializerAdapter.createDefault();
    }

    ListBlobsFlatSegmentResponse listBlobFlatSegment(String pageId,
                                                     String containerName,
                                                     ListBlobsOptions options) {
        options = options == null ? new ListBlobsOptions() : options;

        ContainersListBlobFlatSegmentResponse response = this.listBlobFlatSegmentWithRestResponse(pageId,
            containerName,
            options.getPrefix(),
            options.getMaxResultsPerPage(),
            options.getDetails().toList(),
            null,
            null,
            CancellationToken.NONE);

        return response.getValue();
    }

    void listBlobFlatSegment(String pageId,
                             String containerName,
                             ListBlobsOptions options,
                             CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders> callback) {
        options = options == null ? new ListBlobsOptions() : options;

        this.listBlobFlatSegment(pageId,
            containerName,
            options.getPrefix(),
            options.getMaxResultsPerPage(),
            options.getDetails().toList(),
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    ContainersListBlobFlatSegmentResponse listBlobFlatSegmentWithRestResponse(String pageId,
                                                                              String containerName,
                                                                              String prefix,
                                                                              Integer maxResults,
                                                                              List<ListBlobsIncludeItem> include,
                                                                              Integer timeout,
                                                                              String requestId,
                                                                              CancellationToken cancellationToken) {
        return this.listBlobFlatSegmentWithRestResponseIntern(pageId, containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            cancellationToken,
            null);
    }

    void listBlobFlatSegment(String pageId,
                             String containerName,
                             String prefix,
                             Integer maxResults,
                             List<ListBlobsIncludeItem> include,
                             Integer timeout,
                             String requestId,
                             CancellationToken cancellationToken,
                             CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders> callback) {
        this.listBlobFlatSegmentWithRestResponseIntern(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            cancellationToken,
            callback);
    }

    /**
     * Reads the blob's metadata & properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's metadata.
     */
    BlobGetPropertiesHeaders getBlobProperties(String containerName,
                                               String blobName) {
        BlobGetPropertiesResponse blobGetPropertiesResponse = getBlobPropertiesWithRestResponse(containerName,
            blobName,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE);

        return blobGetPropertiesResponse.getDeserializedHeaders();
    }

    /**
     * Reads the blob's metadata & properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    void getBlobProperties(String containerName,
                           String blobName,
                           CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param snapshot      he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo       Additional parameters for the operation.
     * @return A response containing the blob metadata.
     */
    BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                String blobName,
                                                                String snapshot,
                                                                Integer timeout,
                                                                String version,
                                                                String leaseId,
                                                                String requestId,
                                                                CpkInfo cpkInfo,
                                                                CancellationToken cancellationToken) {
        return getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            leaseId,
            requestId,
            cpkInfo,
            cancellationToken,
            null);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param snapshot      he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo       Additional parameters for the operation.
     * @param callback      Callback that receives the response.
     */
    void getBlobProperties(String containerName,
                           String blobName,
                           String snapshot,
                           Integer timeout,
                           String version,
                           String leaseId,
                           String requestId,
                           CpkInfo cpkInfo,
                           CancellationToken cancellationToken,
                           CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        this.getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            leaseId,
            requestId,
            cpkInfo,
            cancellationToken,
            callback);
    }

    Void createContainer(String containerName) {
        return createContainerWithRestResponse(containerName,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE).getValue();
    }

    void createContainer(String containerName,
                           CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        createContainersWithRestResponseIntern(containerName,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    ContainerCreateResponse createContainerWithRestResponse(String containerName,
                                                                Integer timeout,
                                                                Map<String, String> metadata,
                                                                PublicAccessType publicAccessType,
                                                                String version,
                                                                String requestId,
                                                                CancellationToken cancellationToken) {
        return createContainersWithRestResponseIntern(containerName,
            timeout,
            metadata,
            publicAccessType,
            version,
            requestId,
            cancellationToken,
            null);
    }

    void createContainer(String containerName,
                           Integer timeout,
                           Map<String, String> metadata,
                           PublicAccessType publicAccessType,
                           String version,
                           String requestId,
                           CancellationToken cancellationToken,
                           CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        this.createContainersWithRestResponseIntern(containerName,
            timeout,
            metadata,
            publicAccessType,
            version,
            requestId,
            cancellationToken,
            callback);
    }

    /**
     * Gets the container's properties.
     *
     * @param containerName The container name.
     * @return The container's properties.
     */
    ContainerGetPropertiesHeaders getContainerProperties(String containerName) {
        ContainerGetPropertiesResponse response = getContainerPropertiesWithResponse(containerName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE);

        return response.getDeserializedHeaders();
    }

    /**
     * Gets the container's properties.
     *
     * @param containerName The container name.
     * @param callback      Callback that receives the response.
     */
    void getContainerProperties(String containerName,
                                CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        getContainerPropertiesWithRestResponseIntern(containerName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    /**
     * Gets the container's properties..
     *
     * @param containerName The container name.
     *  @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @return A response containing the blob metadata.
     */
    ContainerGetPropertiesResponse getContainerPropertiesWithResponse(String containerName,
                                                                      Integer timeout,
                                                                      String version,
                                                                      String leaseId,
                                                                      String requestId,
                                                                      CancellationToken cancellationToken) {
        return getContainerPropertiesWithRestResponseIntern(containerName,
            timeout,
            version,
            leaseId,
            requestId,
            cancellationToken,
            null);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param callback      Callback that receives the response.
     */
    void getContainerProperties(String containerName,
                                Integer timeout,
                                String version,
                                String leaseId,
                                String requestId,
                                CancellationToken cancellationToken,
                                CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        this.getContainerPropertiesWithRestResponseIntern(containerName,
            timeout,
            version,
            leaseId,
            requestId,
            cancellationToken,
            callback);
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return A response containing the blob data.
     */
    ResponseBody download(String containerName,
                          String blobName) {
        return downloadWithRestResponse(containerName,
            blobName,
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
            null,
            null,
            null,
            CancellationToken.NONE).getValue();
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    void download(String containerName,
                  String blobName,
                  CallbackWithHeader<ResponseBody, BlobDownloadHeaders> callback) {
        rawDownload(containerName,
            blobName,
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
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    /**
     * The Download operation reads or downloads a blob from the system, including its metadata and properties. You
     * can also call Download to read a snapshot or version.
     *
     * @param containerName        The container name.
     * @param blobName             The blob name.
     * @param snapshot             The snapshot parameter is an opaque DateTime value that, when present, specifies
     *                             the blob snapshot to retrieve. For more information on working with blob
     *                             snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout              The timeout parameter is expressed in seconds. For more information, see
     *                             &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param range                Return only the bytes of the blob in the specified range.
     * @param leaseId              If specified, the operation only succeeds if the resource's lease is active and
     *                             matches this ID.
     * @param rangeGetContentMd5   When set to true and specified together with the Range, the service returns the
     *                             MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param rangeGetContentCrc64 When set to true and specified together with the Range, the service returns the
     *                             CRC64 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param ifModifiedSince      The datetime that resources must have been modified since.
     * @param ifUnmodifiedSince    The datetime that resources must have remained unmodified since.
     * @param ifMatch              Specify an ETag value to operate only on blobs with a matching value.
     * @param ifNoneMatch          Specify an ETag value to operate only on blobs without a matching value.
     * @param version              Specifies the version of the operation to use for this request.
     * @param requestId            Provides a client-generated, opaque value with a 1 KB character limit that is
     *                             recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo              Additional parameters for the operation.
     * @return A response containing the blob data.
     */
    BlobDownloadResponse downloadWithRestResponse(String containerName,
                                                  String blobName,
                                                  String snapshot,
                                                  Integer timeout,
                                                  String range,
                                                  String leaseId,
                                                  Boolean rangeGetContentMd5,
                                                  Boolean rangeGetContentCrc64,
                                                  OffsetDateTime ifModifiedSince,
                                                  OffsetDateTime ifUnmodifiedSince,
                                                  String ifMatch,
                                                  String ifNoneMatch,
                                                  String version,
                                                  String requestId,
                                                  CpkInfo cpkInfo,
                                                  CancellationToken cancellationToken) {
        return downloadWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            range,
            leaseId,
            rangeGetContentMd5,
            rangeGetContentCrc64,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            version,
            requestId,
            cpkInfo,
            cancellationToken,
            null);
    }

    /**
     * The Download operation reads or downloads a blob from the system, including its metadata and properties. You
     * can also call Download to read a snapshot or version.
     *
     * @param containerName        The container name.
     * @param blobName             The blob name.
     * @param snapshot             he snapshot parameter is an opaque DateTime value that, when present, specifies
     *                             the blob snapshot to retrieve. For more information on working with blob
     *                             snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout              The timeout parameter is expressed in seconds. For more information, see
     *                             &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param range                Return only the bytes of the blob in the specified range.
     * @param leaseId              If specified, the operation only succeeds if the resource's lease is active and
     *                             matches this ID.
     * @param rangeGetContentMD5   When set to true and specified together with the Range, the service returns the
     *                             MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param rangeGetContentCRC64 When set to true and specified together with the Range, the service returns the
     *                             CRC64 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param ifModifiedSince      The datetime that resources must have been modified since.
     * @param ifUnmodifiedSince    The datetime that resources must have remained unmodified since.
     * @param ifMatch              Specify an ETag value to operate only on blobs with a matching value.
     * @param ifNoneMatch          Specify an ETag value to operate only on blobs without a matching value.
     * @param version              Specifies the version of the operation to use for this request.
     * @param requestId            Provides a client-generated, opaque value with a 1 KB character limit that is
     *                             recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo              Additional parameters for the operation.
     * @param callback             Callback that receives the response.
     */
    void rawDownload(String containerName,
                     String blobName,
                     String snapshot,
                     Integer timeout,
                     String range,
                     String leaseId,
                     Boolean rangeGetContentMD5,
                     Boolean rangeGetContentCRC64,
                     OffsetDateTime ifModifiedSince,
                     OffsetDateTime ifUnmodifiedSince,
                     String ifMatch,
                     String ifNoneMatch,
                     String version,
                     String requestId,
                     CpkInfo cpkInfo,
                     CancellationToken cancellationToken,
                     CallbackWithHeader<ResponseBody, BlobDownloadHeaders> callback) {
        this.downloadWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            range,
            leaseId,
            rangeGetContentMD5,
            rangeGetContentCRC64,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            version,
            requestId,
            cpkInfo,
            cancellationToken,
            callback);
    }

    Void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    byte[] blockContent,
                    byte[] contentMd5) {
        return this.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE).getValue();
    }

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    byte[] blockContent,
                    byte[] contentMd5,
                    CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlock(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    BlockBlobsStageBlockResponse stageBlockWithRestResponse(String containerName,
                                                            String blobName,
                                                            String base64BlockId,
                                                            byte[] blockContent,
                                                            byte[] transactionalContentMD5,
                                                            byte[] transactionalContentCrc64,
                                                            Integer timeout,
                                                            String leaseId,
                                                            String requestId,
                                                            CpkInfo cpkInfo,
                                                            CancellationToken cancellationToken) {
        return this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            cancellationToken,
            null);
    }

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    byte[] blockContent,
                    byte[] transactionalContentMD5,
                    byte[] transactionalContentCrc64,
                    Integer timeout,
                    String leaseId,
                    String requestId,
                    CpkInfo cpkInfo,
                    CancellationToken cancellationToken,
                    CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            cancellationToken,
            callback);
    }

    BlockBlobItem commitBlockList(String containerName,
                                  String blobName,
                                  List<String> base64BlockIds,
                                  boolean overwrite) {
        BlobRequestConditions requestConditions = null;

        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
        }

        BlockBlobsCommitBlockListResponse response = this.commitBlockListWithRestResponse(containerName,
            blobName,
            base64BlockIds,
            null,
            null,
            null,
            null,
            null,
            requestConditions,
            null,
            null,
            null,
            CancellationToken.NONE);

        return response.getBlockBlobItem();
    }

    void commitBlockList(String containerName,
                         String blobName,
                         List<String> base64BlockIds,
                         boolean overwrite,
                         CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders> callback) {
        BlobRequestConditions requestConditions = null;

        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
        }

        this.commitBlockList(containerName,
            blobName,
            base64BlockIds,
            null,
            null,
            null,
            null,
            null,
            requestConditions,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    BlockBlobsCommitBlockListResponse commitBlockListWithRestResponse(String containerName,
                                                                      String blobName,
                                                                      List<String> base64BlockIds,
                                                                      byte[] transactionalContentMD5,
                                                                      byte[] transactionalContentCrc64,
                                                                      Integer timeout,
                                                                      BlobHttpHeaders blobHttpHeaders,
                                                                      Map<String, String> metadata,
                                                                      BlobRequestConditions requestConditions,
                                                                      String requestId,
                                                                      CpkInfo cpkInfo,
                                                                      AccessTier tier,
                                                                      CancellationToken cancellationToken) {
        return this.commitBlockListWithRestResponseIntern(containerName,
            blobName,
            base64BlockIds,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            blobHttpHeaders,
            metadata,
            requestConditions,
            requestId,
            cpkInfo,
            tier,
            cancellationToken,
            null);
    }

    void commitBlockList(String containerName,
                         String blobName,
                         List<String> base64BlockIds,
                         byte[] transactionalContentMD5,
                         byte[] transactionalContentCrc64,
                         Integer timeout,
                         BlobHttpHeaders blobHttpHeaders,
                         Map<String, String> metadata,
                         BlobRequestConditions requestConditions,
                         String requestId,
                         CpkInfo cpkInfo,
                         AccessTier tier,
                         CancellationToken cancellationToken,
                         CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders> callback) {
        this.commitBlockListWithRestResponseIntern(containerName,
            blobName,
            base64BlockIds,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            blobHttpHeaders,
            metadata,
            requestConditions,
            requestId,
            cpkInfo,
            tier,
            cancellationToken,
            callback);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    Void deleteBlob(String containerName,
                    String blobName) {
        return deleteBlobWithRestResponse(containerName,
            blobName,
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
            CancellationToken.NONE).getValue();
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     * @return A handle to the service call.
     */
    void deleteBlob(String containerName,
                    String blobName,
                    CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        deleteBlob(containerName,
            blobName,
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
            callback);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     * <p>
     * If the storage account's soft delete feature is disabled then, when a blob is deleted, it is permanently
     * removed from the storage account. If the storage account's soft delete feature is enabled, then, when a blob
     * is deleted, it is marked for deletion and becomes inaccessible immediately. However, the blob service retains
     * the blob or snapshot for the number of days specified by the DeleteRetentionPolicy section of
     * &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties"&gt; Storage service properties.&lt;/a&gt;.
     * After the specified number of days has passed, the blob's data is permanently removed from the storage account.
     * Note that you continue to be charged for the soft-deleted blob's storage until it is permanently removed. Use
     * the List Blobs API and specify the "include=deleted" query parameter to discover which blobs and snapshots
     * have been soft deleted. You can then use the Undelete Blob API to restore a soft-deleted blob. All other
     * operations on a soft-deleted blob or snapshot causes the service to return an HTTP status code of 404
     * (ResourceNotFound). If the storage account's automatic snapshot feature is enabled, then, when a blob is
     * deleted, an automatic snapshot is created. The blob becomes inaccessible immediately. All other operations on
     * the blob causes the service to return an HTTP status code of 404 (ResourceNotFound). You can access automatic
     * snapshot using snapshot timestamp or version ID. You can restore the blob by calling Put or Copy Blob API with
     * automatic snapshot as source. Deleting automatic snapshot requires shared key or special SAS/RBAC permissions.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the
     *                          blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId           If specified, the operation only succeeds if the resource's lease is active and
     *                          matches this ID.
     * @param deleteSnapshots   Required if the blob has associated snapshots. Specify one of the following two
     *                          options: include: Delete the base blob and all of its snapshots. only: Delete only the blob's snapshots and not the blob itself. Possible values include: 'include', 'only'.
     * @param ifModifiedSince   Specify this header value to operate only on a blob if it has been modified since the
     *                          specified date/time.
     * @param ifUnmodifiedSince Specify this header value to operate only on a blob if it has not been modified since
     *                          the specified date/time.
     * @param ifMatch           Specify an ETag value to operate only on blobs with a matching value.
     * @param ifNoneMatch       Specify an ETag value to operate only on blobs without a matching value.
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is
     *                          recorded in the analytics logs when storage analytics logging is enabled.
     * @return A response object containing the details of the delete operation.
     */
    BlobDeleteResponse deleteBlobWithRestResponse(String containerName,
                                                  String blobName,
                                                  String snapshot,
                                                  Integer timeout,
                                                  String version,
                                                  String leaseId,
                                                  DeleteSnapshotsOptionType deleteSnapshots,
                                                  OffsetDateTime ifModifiedSince,
                                                  OffsetDateTime ifUnmodifiedSince,
                                                  String ifMatch,
                                                  String ifNoneMatch,
                                                  String requestId,
                                                  CancellationToken cancellationToken) {
        return deleteBlobWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            leaseId,
            deleteSnapshots,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            requestId,
            cancellationToken,
            null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     * <p>
     * If the storage account's soft delete feature is disabled then, when a blob is deleted, it is permanently
     * removed from the storage account. If the storage account's soft delete feature is enabled, then, when a blob
     * is deleted, it is marked for deletion and becomes inaccessible immediately. However, the blob service retains
     * the blob or snapshot for the number of days specified by the DeleteRetentionPolicy section of
     * &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties"&gt; Storage service properties.&lt;/a&gt;.
     * After the specified number of days has passed, the blob's data is permanently removed from the storage account.
     * Note that you continue to be charged for the soft-deleted blob's storage until it is permanently removed. Use
     * the List Blobs API and specify the "include=deleted" query parameter to discover which blobs and snapshots
     * have been soft deleted. You can then use the Undelete Blob API to restore a soft-deleted blob. All other
     * operations on a soft-deleted blob or snapshot causes the service to return an HTTP status code of 404
     * (ResourceNotFound). If the storage account's automatic snapshot feature is enabled, then, when a blob is
     * deleted, an automatic snapshot is created. The blob becomes inaccessible immediately. All other operations on
     * the blob causes the service to return an HTTP status code of 404 (ResourceNotFound). You can access automatic
     * snapshot using snapshot timestamp or version ID. You can restore the blob by calling Put or Copy Blob API with
     * automatic snapshot as source. Deleting automatic snapshot requires shared key or special SAS/RBAC permissions.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the
     *                          blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId           If specified, the operation only succeeds if the resource's lease is active and
     *                          matches this ID.
     * @param deleteSnapshots   Required if the blob has associated snapshots. Specify one of the following two
     *                          options: include: Delete the base blob and all of its snapshots. only: Delete only the blob's snapshots and not the blob itself. Possible values include: 'include', 'only'.
     * @param ifModifiedSince   Specify this header value to operate only on a blob if it has been modified since the
     *                          specified date/time.
     * @param ifUnmodifiedSince Specify this header value to operate only on a blob if it has not been modified since
     *                          the specified date/time.
     * @param ifMatch           Specify an ETag value to operate only on blobs with a matching value.
     * @param ifNoneMatch       Specify an ETag value to operate only on blobs without a matching value.
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is
     *                          recorded in the analytics logs when storage analytics logging is enabled.
     * @param callback          Callback that receives the response.
     * @return A handle to the service call.
     */
    void deleteBlob(String containerName,
                    String blobName,
                    String snapshot,
                    Integer timeout,
                    String version,
                    String leaseId,
                    DeleteSnapshotsOptionType deleteSnapshots,
                    OffsetDateTime ifModifiedSince,
                    OffsetDateTime ifUnmodifiedSince,
                    String ifMatch,
                    String ifNoneMatch,
                    String requestId,
                    CancellationToken cancellationToken,
                    CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        deleteBlobWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            leaseId,
            deleteSnapshots,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            requestId,
            cancellationToken,
            callback);
    }

    /**
     * Deletes a container.
     *
     * @param containerName The container name.
     */
    Void deleteContainer(String containerName) {
        return deleteContainerWithRestResponse(containerName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE).getValue();
    }

    /**
     * Deletes a container.
     *
     * @param containerName The container name.
     * @param callback      Callback that receives the response.
     * @return A handle to the service call.
     */
    void deleteContainer(String containerName,
                         CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        deleteContainer(containerName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    /**
     * Deletes a container.
     *
     * @param containerName     The container name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is
     *                          recorded in the analytics logs when storage analytics logging is enabled.
     * @return A response object containing the details of the delete operation.
     */
    ContainerDeleteResponse deleteContainerWithRestResponse(String containerName,
                                                            Integer timeout,
                                                            String version,
                                                            BlobRequestConditions requestConditions,
                                                            String requestId,
                                                            CancellationToken cancellationToken) {
        return deleteContainerWithRestResponseIntern(containerName,
            timeout,
            version,
            requestConditions,
            requestId,
            cancellationToken,
            null);
    }

    /**
     * Deletes a container.
     *
     * @param containerName     The container name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is
     *                          recorded in the analytics logs when storage analytics logging is enabled.
     * @param callback          Callback that receives the response.
     */
    void deleteContainer(String containerName,
                         Integer timeout,
                         String version,
                         BlobRequestConditions requestConditions,
                         String requestId,
                         CancellationToken cancellationToken,
                         CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        deleteContainerWithRestResponseIntern(containerName,
            timeout,
            version,
            requestConditions,
            requestId,
            cancellationToken,
            callback);
    }

    private ContainerCreateResponse createContainersWithRestResponseIntern(String containerName,
                                                                           Integer timeout,
                                                                           Map<String, String> metadata,
                                                                           PublicAccessType publicAccessType,
                                                                           String version,
                                                                           String requestId,
                                                                           CancellationToken cancellationToken,
                                                                           CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        Call<ResponseBody> call = service.createContainer(containerName,
            timeout,
            metadata == null ? null : new MetadataInterceptor.StorageMultiHeaders(metadata),
            publicAccessType,
            XMS_VERSION, // TODO: Replace with 'version'.
            requestId,
            "container",
            null, // TODO: Add cpk stuff?
            null);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            ContainerCreateHeaders typedHeaders = deserializeHeaders(response.headers(),
                                ContainerCreateHeaders.class);

                            callback.onSuccess(null, typedHeaders, response.raw());
                        } else {
                            callback.onFailure(new BlobStorageException(null, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    ContainerCreateHeaders headers = deserializeHeaders(response.headers(),
                        ContainerCreateHeaders.class);

                    ContainerCreateResponse result = new ContainerCreateResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        headers);

                    return result;
                } else {
                    throw new BlobStorageException(null, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }


    private ContainersListBlobFlatSegmentResponse listBlobFlatSegmentWithRestResponseIntern(String pageId,
                                                                                            String containerName,
                                                                                            String prefix,
                                                                                            Integer maxResults,
                                                                                            List<ListBlobsIncludeItem> include,
                                                                                            Integer timeout,
                                                                                            String requestId,
                                                                                            CancellationToken cancellationToken,
                                                                                            CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String resType = "container";
        final String comp = "list";

        Call<ResponseBody> call = service.listBlobFlatSegment(containerName,
            prefix,
            pageId,
            maxResults,
            this.serializerAdapter.serializeList(include, SerializerAdapter.CollectionFormat.CSV),
            timeout,
            XMS_VERSION,
            requestId,
            resType,
            comp);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            ListBlobsFlatSegmentResponse typedContent = deserializeContent(response.body(),
                                ListBlobsFlatSegmentResponse.class);
                            ListBlobFlatSegmentHeaders typedHeader = deserializeHeaders(response.headers(),
                                ListBlobFlatSegmentHeaders.class);
                            callback.onSuccess(typedContent,
                                typedHeader,
                                response.raw());
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    ListBlobsFlatSegmentResponse typedContent = deserializeContent(response.body(),
                        ListBlobsFlatSegmentResponse.class);
                    ListBlobFlatSegmentHeaders typedHeader = deserializeHeaders(response.headers(),
                        ListBlobFlatSegmentHeaders.class);

                    ContainersListBlobFlatSegmentResponse result =
                        new ContainersListBlobFlatSegmentResponse(response.raw().request(),
                            response.code(),
                            response.headers(),
                            typedContent,
                            typedHeader);

                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private BlobGetPropertiesResponse getBlobPropertiesWithRestResponseIntern(String containerName,
                                                                              String blobName,
                                                                              String snapshot,
                                                                              Integer timeout,
                                                                              String version,
                                                                              String leaseId,
                                                                              String requestId,
                                                                              CpkInfo cpkInfo,
                                                                              CancellationToken cancellationToken,
                                                                              CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        String encryptionKey = null;
        String encryptionKeySha256 = null;
        EncryptionAlgorithmType encryptionAlgorithm = null;

        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }

        Call<Void> call = service.getBlobProperties(containerName,
            blobName,
            snapshot,
            timeout,
            XMS_VERSION, // TODO: Replace with 'version'.
            leaseId,
            requestId,
            encryptionKey,
            encryptionKeySha256,
            encryptionAlgorithm);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            BlobGetPropertiesHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobGetPropertiesHeaders.class);

                            callback.onSuccess(null, typedHeaders, response.raw());
                        } else {
                            callback.onFailure(new BlobStorageException(null, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<Void> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    BlobGetPropertiesHeaders headers = deserializeHeaders(response.headers(),
                        BlobGetPropertiesHeaders.class);

                    BlobGetPropertiesResponse result = new BlobGetPropertiesResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        headers);

                    return result;
                } else {
                    throw new BlobStorageException(null, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private ContainerGetPropertiesResponse getContainerPropertiesWithRestResponseIntern(String containerName,
                                                                                        Integer timeout,
                                                                                        String version,
                                                                                        String leaseId,
                                                                                        String requestId,
                                                                                        CancellationToken cancellationToken,
                                                                                        CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String restype = "container";

        Call<Void> call = service.getContainerProperties(containerName,
            timeout,
            XMS_VERSION, // TODO: Replace with 'version'.
            leaseId,
            requestId,
            restype);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            ContainerGetPropertiesHeaders typedHeaders = deserializeHeaders(response.headers(),
                                ContainerGetPropertiesHeaders.class);

                            callback.onSuccess(null, typedHeaders, response.raw());
                        } else {
                            callback.onFailure(new BlobStorageException(null, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<Void> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    ContainerGetPropertiesHeaders headers = deserializeHeaders(response.headers(),
                        ContainerGetPropertiesHeaders.class);

                    ContainerGetPropertiesResponse result = new ContainerGetPropertiesResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        headers);

                    return result;
                } else {
                    throw new BlobStorageException(null, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private BlobDownloadResponse downloadWithRestResponseIntern(String containerName,
                                                                String blobName,
                                                                String snapshot,
                                                                Integer timeout,
                                                                String range,
                                                                String leaseId,
                                                                Boolean rangeGetContentMd5,
                                                                Boolean rangeGetContentCrc64,
                                                                OffsetDateTime ifModifiedSince,
                                                                OffsetDateTime ifUnmodifiedSince,
                                                                String ifMatch,
                                                                String ifNoneMatch,
                                                                String version,
                                                                String requestId,
                                                                CpkInfo cpkInfo,
                                                                CancellationToken cancellationToken,
                                                                CallbackWithHeader<ResponseBody, BlobDownloadHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        String encryptionKey = null;
        String encryptionKeySha256 = null;
        EncryptionAlgorithmType encryptionAlgorithm = null;

        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }

        DateTimeRfc1123 ifModifiedSinceConverted = ifModifiedSince == null ? null :
            new DateTimeRfc1123(ifModifiedSince);
        DateTimeRfc1123 ifUnmodifiedSinceConverted = ifUnmodifiedSince == null ? null :
            new DateTimeRfc1123(ifUnmodifiedSince);

        Call<ResponseBody> call = service.download(containerName,
            blobName,
            snapshot,
            timeout,
            range,
            leaseId,
            rangeGetContentMd5,
            rangeGetContentCrc64,
            ifModifiedSinceConverted,
            ifUnmodifiedSinceConverted,
            ifMatch,
            ifNoneMatch,
            XMS_VERSION, // TODO: Replace with 'version'.
            requestId,
            encryptionKey,
            encryptionKeySha256,
            encryptionAlgorithm);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200 || response.code() == 206) {
                            BlobDownloadHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobDownloadHeaders.class);

                            callback.onSuccess(response.body(),
                                typedHeaders,
                                response.raw());
                        } else {
                            String strContent = readAsString(response.body());
                            callback.onFailure(new BlobStorageException(strContent, response.raw()),response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());
                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200 || response.code() == 206) {
                    BlobDownloadHeaders headers = deserializeHeaders(response.headers(), BlobDownloadHeaders.class);

                    BlobDownloadResponse result = new BlobDownloadResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        response.body(),
                        headers);

                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private BlockBlobsStageBlockResponse stageBlockWithRestResponseIntern(String containerName,
                                                                          String blobName,
                                                                          String base64BlockId,
                                                                          byte[] blockContent,
                                                                          byte[] transactionalContentMD5,
                                                                          byte[] transactionalContentCrc64,
                                                                          Integer timeout,
                                                                          String leaseId,
                                                                          String requestId,
                                                                          CpkInfo cpkInfo,
                                                                          CancellationToken cancellationToken,
                                                                          CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        String encryptionKey = null;
        String encryptionKeySha256 = null;
        EncryptionAlgorithmType encryptionAlgorithm = null;
        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }
        //
        final String comp = "block";
        String transactionalContentMD5Converted = Base64Util.encodeToString(transactionalContentMD5);
        String transactionalContentCrc64Converted = Base64Util.encodeToString(transactionalContentCrc64);
        //
        int contentLength = blockContent.length;
        RequestBody body = RequestBody.create(MediaType.get("application/octet-stream"), blockContent);

        Call<ResponseBody> call = service.stageBlock(containerName,
            blobName,
            base64BlockId,
            contentLength,
            transactionalContentMD5Converted,
            transactionalContentCrc64Converted,
            body,
            timeout,
            leaseId,
            XMS_VERSION,
            requestId,
            comp,
            encryptionKey,
            encryptionKeySha256,
            encryptionAlgorithm);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            BlockBlobStageBlockHeaders typedHeader = deserializeHeaders(response.headers(),
                                BlockBlobStageBlockHeaders.class);
                            callback.onSuccess(null, typedHeader, response.raw());
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure(t, null);
                }
            });
            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    BlockBlobStageBlockHeaders typedHeader = deserializeHeaders(response.headers(),
                        BlockBlobStageBlockHeaders.class);

                    BlockBlobsStageBlockResponse result = new BlockBlobsStageBlockResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        typedHeader);
                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private BlockBlobsCommitBlockListResponse commitBlockListWithRestResponseIntern(String containerName,
                                                                                    String blobName,
                                                                                    List<String> base64BlockIds,
                                                                                    byte[] transactionalContentMD5,
                                                                                    byte[] transactionalContentCrc64,
                                                                                    Integer timeout,
                                                                                    BlobHttpHeaders blobHttpHeaders,
                                                                                    Map<String, String> metadata,
                                                                                    BlobRequestConditions requestConditions,
                                                                                    String requestId,
                                                                                    CpkInfo cpkInfo,
                                                                                    AccessTier tier,
                                                                                    CancellationToken cancellationToken,
                                                                                    CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        String leaseId = requestConditions.getLeaseId();
        DateTimeRfc1123 ifModifiedSince = requestConditions.getIfModifiedSince() == null
            ? null :
            new DateTimeRfc1123(requestConditions.getIfModifiedSince());
        DateTimeRfc1123 ifUnmodifiedSince = requestConditions.getIfUnmodifiedSince() == null
            ? null :
            new DateTimeRfc1123(requestConditions.getIfUnmodifiedSince());
        String ifMatch = requestConditions.getIfMatch();
        String ifNoneMatch = requestConditions.getIfNoneMatch();
        String cacheControl = null;

        if (blobHttpHeaders != null) {
            cacheControl = blobHttpHeaders.getCacheControl();
        }

        String contentType = null;

        if (blobHttpHeaders != null) {
            contentType = blobHttpHeaders.getContentType();
        }

        String contentEncoding = null;

        if (blobHttpHeaders != null) {
            contentEncoding = blobHttpHeaders.getContentEncoding();
        }

        String contentLanguage = null;

        if (blobHttpHeaders != null) {
            contentLanguage = blobHttpHeaders.getContentLanguage();
        }

        byte[] contentMd5 = null;

        if (blobHttpHeaders != null) {
            contentMd5 = blobHttpHeaders.getContentMd5();
        }

        String contentDisposition = null;

        if (blobHttpHeaders != null) {
            contentDisposition = blobHttpHeaders.getContentDisposition();
        }

        String encryptionKey = null;

        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
        }

        String encryptionKeySha256 = null;

        if (cpkInfo != null) {
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
        }

        EncryptionAlgorithmType encryptionAlgorithm = null;

        if (cpkInfo != null) {
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }

        final String comp = "blocklist";
        String transactionalContentMD5Converted = Base64Util.encodeToString(transactionalContentMD5);
        String transactionalContentCrc64Converted = Base64Util.encodeToString(transactionalContentCrc64);
        String contentMd5Converted = Base64Util.encodeToString(contentMd5);

        BlockLookupList blockLookupList = new BlockLookupList().setLatest(base64BlockIds);
        final RequestBody blocks;

        try {
            blocks = RequestBody.create(MediaType.get("application/xml; charset=utf-8"),
                serializerAdapter.serialize(blockLookupList, SerializerFormat.XML));
        } catch (IOException ioe) {
            if (callback != null) {
                callback.onFailure(ioe, null);

                return null;
            } else {
                throw new RuntimeException(ioe);
            }
        }

        Call<ResponseBody> call = service.commitBlockList(containerName,
            blobName,
            timeout,
            transactionalContentMD5Converted,
            transactionalContentCrc64Converted,
            metadata,
            leaseId,
            tier,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            blocks,
            XMS_VERSION,
            requestId,
            comp,
            cacheControl,
            contentType,
            contentEncoding,
            contentLanguage,
            contentMd5Converted,
            contentDisposition,
            encryptionKey,
            encryptionKeySha256,
            encryptionAlgorithm);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            BlockBlobCommitBlockListHeaders typedHeader =
                                deserializeHeaders(response.headers(), BlockBlobCommitBlockListHeaders.class);

                            callback.onSuccess(new BlockBlobItem(typedHeader.getETag(),
                                    typedHeader.getLastModified(),
                                    typedHeader.getContentMD5(),
                                    typedHeader.isServerEncrypted(),
                                    typedHeader.getEncryptionKeySha256()),
                                typedHeader, response.raw());
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure(t, null);
                }
            });
            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    BlockBlobCommitBlockListHeaders typedHeader =
                        deserializeHeaders(response.headers(), BlockBlobCommitBlockListHeaders.class);

                    BlockBlobsCommitBlockListResponse result =
                        new BlockBlobsCommitBlockListResponse(response.raw().request(),
                            response.code(),
                            response.headers(),
                            null,
                            typedHeader);

                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private BlobDeleteResponse deleteBlobWithRestResponseIntern(String containerName,
                                                                String blobName,
                                                                String snapshot,
                                                                Integer timeout,
                                                                String version,
                                                                String leaseId,
                                                                DeleteSnapshotsOptionType deleteSnapshots,
                                                                OffsetDateTime ifModifiedSince,
                                                                OffsetDateTime ifUnmodifiedSince,
                                                                String ifMatch,
                                                                String ifNoneMatch,
                                                                String requestId,
                                                                CancellationToken cancellationToken,
                                                                CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        DateTimeRfc1123 ifModifiedSinceConverted = ifModifiedSince == null ? null :
            new DateTimeRfc1123(ifModifiedSince);
        DateTimeRfc1123 ifUnmodifiedSinceConverted = ifUnmodifiedSince == null ? null :
            new DateTimeRfc1123(ifUnmodifiedSince);

        Call<ResponseBody> call = service.deleteBlob(containerName,
            blobName,
            snapshot,
            timeout,
            leaseId,
            deleteSnapshots,
            ifModifiedSinceConverted,
            ifUnmodifiedSinceConverted,
            ifMatch,
            ifNoneMatch,
            XMS_VERSION, // TODO: Replace with 'version'.
            requestId);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 202) {
                            BlobDeleteHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobDeleteHeaders.class);

                            callback.onSuccess(null, typedHeaders, response.raw());
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 202) {
                    BlobDeleteHeaders headers = deserializeHeaders(response.headers(),
                        BlobDeleteHeaders.class);

                    BlobDeleteResponse result = new BlobDeleteResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        headers);

                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private ContainerDeleteResponse deleteContainerWithRestResponseIntern(String containerName,
                                                                          Integer timeout,
                                                                          String version,
                                                                          BlobRequestConditions requestConditions,
                                                                          String requestId,
                                                                          CancellationToken cancellationToken,
                                                                          CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String restype = "container";
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;

        DateTimeRfc1123 ifModifiedSinceConverted = requestConditions.getIfModifiedSince() == null ? null :
            new DateTimeRfc1123(requestConditions.getIfModifiedSince());
        DateTimeRfc1123 ifUnmodifiedSinceConverted = requestConditions.getIfUnmodifiedSince() == null ? null :
            new DateTimeRfc1123(requestConditions.getIfUnmodifiedSince());

        Call<ResponseBody> call = service.deleteContainer(containerName,
            restype,
            timeout,
            requestConditions.getLeaseId(),
            ifModifiedSinceConverted,
            ifUnmodifiedSinceConverted,
            XMS_VERSION, // TODO: Replace with 'version'.
            requestId);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 202) {
                            ContainerDeleteHeaders typedHeaders = deserializeHeaders(response.headers(),
                                ContainerDeleteHeaders.class);

                            callback.onSuccess(null, typedHeaders, response.raw());
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()), response.raw());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 202) {
                    ContainerDeleteHeaders headers = deserializeHeaders(response.headers(),
                        ContainerDeleteHeaders.class);

                    ContainerDeleteResponse result = new ContainerDeleteResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        headers);

                    return result;
                } else {
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else {
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private static <T> Response<T> executeCall(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> void executeCall(Call<T> call, retrofit2.Callback<T> callback) {
        call.enqueue(callback);
    }

    private static String readAsString(ResponseBody body) {
        if (body == null) {
            return "";
        }

        try {
            return new String(body.bytes());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            body.close();
        }
    }

    private <T> T deserializeContent(ResponseBody body, Type type) {
        String str = readAsString(body);
        try {
            return this.serializerAdapter.deserialize(str, type, SerializerFormat.XML);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private <T> T deserializeHeaders(Headers headers, Type type) {
        try {
            return this.serializerAdapter.deserialize(headers, type);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private interface StorageBlobService {
        @PUT("{containerName}")
        Call<ResponseBody> createContainer(@Path("containerName") String containerName,
                                                      @Query("timeout") Integer timeout,
                                                      @Tag MetadataInterceptor.StorageMultiHeaders metadata,
                                                      @Header("x-ms-blob-public-access") PublicAccessType access,
                                                      @Header("x-ms-version") String version,
                                                      @Header("x-ms-client-request-id") String requestId,
                                                      @Query("restype") String restype,
                                                      @Header("x-ms-default-encryption-scope") String defaultEncryptionScope,
                                                      @Header("x-ms-deny-encryption-scope-override") Boolean encryptionScopeOverridePrevented);

        @GET("{containerName}")
        Call<ResponseBody> listBlobFlatSegment(@Path("containerName") String containerName,
                                               @Query("prefix") String prefix,
                                               @Query("marker") String marker,
                                               @Query("maxresults") Integer maxResults,
                                               @Query("include") String include,
                                               @Query("timeout") Integer timeout,
                                               @Header("x-ms-version") String version,
                                               @Header("x-ms-client-request-id") String requestId,
                                               @Query("restype") String resType,
                                               @Query("comp") String comp);

        @HEAD("{containerName}/{blob}")
        Call<Void> getBlobProperties(@Path("containerName") String containerName,
                                     @Path("blob") String blob,
                                     @Query("snapshot") String snapshot,
                                     @Query("timeout") Integer timeout,
                                     @Header("x-ms-version") String version,
                                     @Header("x-ms-lease-id") String leaseId,
                                     @Header("x-ms-client-request-id") String requestId,
                                     @Header("x-ms-encryption-key") String encryptionKey,
                                     @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                     @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);

        @GET("{containerName}")
        Call<Void> getContainerProperties(@Path("containerName") String containerName,
                                          @Query("timeout") Integer timeout,
                                          @Header("x-ms-version") String version,
                                          @Header("x-ms-lease-id") String leaseId,
                                          @Header("x-ms-client-request-id") String requestId,
                                          @Query("restype") String resType);


        @GET("{containerName}/{blob}")
        Call<ResponseBody> download(@Path("containerName") String containerName,
                                    @Path("blob") String blob,
                                    @Query("snapshot") String snapshot,
                                    @Query("timeout") Integer timeout,
                                    @Header("x-ms-range") String range,
                                    @Header("x-ms-lease-id") String leaseId,
                                    @Header("x-ms-range-get-content-md5") Boolean rangeGetContentMd5,
                                    @Header("x-ms-range-get-content-crc64") Boolean rangeGetContentCrc64,
                                    @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                    @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                    @Header("If-Match") String ifMatch,
                                    @Header("If-None-Match") String ifNoneMatch,
                                    @Header("x-ms-version") String version,
                                    @Header("x-ms-client-request-id") String requestId,
                                    @Header("x-ms-encryption-key") String encryptionKey,
                                    @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                    @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);

        @PUT("{containerName}/{blob}")
        Call<ResponseBody> stageBlock(@Path("containerName") String containerName,
                                      @Path("blob") String blob,
                                      @Query("blockid") String blockId,
                                      @Header("Content-Length") long contentLength,
                                      @Header("Content-MD5") String transactionalContentMD5,
                                      @Header("x-ms-content-crc64") String transactionalContentCrc64,
                                      @Body RequestBody blockContent,
                                      @Query("timeout") Integer timeout,
                                      @Header("x-ms-lease-id") String leaseId,
                                      @Header("x-ms-version") String version,
                                      @Header("x-ms-client-request-id") String requestId,
                                      @Query("comp") String comp,
                                      @Header("x-ms-encryption-key") String encryptionKey,
                                      @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                      @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);

        @PUT("{containerName}/{blob}")
        Call<ResponseBody> commitBlockList(@Path("containerName") String containerName,
                                           @Path("blob") String blobName,
                                           @Query("timeout") Integer timeout,
                                           @Header("Content-MD5") String transactionalContentMD5,
                                           @Header("x-ms-content-crc64") String transactionalContentCrc64,
                                           @Header("x-ms-meta-") Map<String, String> metadata,
                                           @Header("x-ms-lease-id") String leaseId,
                                           @Header("x-ms-access-tier") AccessTier tier,
                                           @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                           @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                           @Header("If-Match") String ifMatch,
                                           @Header("If-None-Match") String ifNoneMatch,
                                           @Body RequestBody blocks,
                                           @Header("x-ms-version") String version,
                                           @Header("x-ms-client-request-id") String requestId,
                                           @Query("comp") String comp,
                                           @Header("x-ms-blob-cache-control") String cacheControl,
                                           @Header("x-ms-blob-content-type") String contentType,
                                           @Header("x-ms-blob-content-encoding") String contentEncoding,
                                           @Header("x-ms-blob-content-language") String contentLanguage,
                                           @Header("x-ms-blob-content-md5") String contentMd5,
                                           @Header("x-ms-blob-content-disposition") String contentDisposition,
                                           @Header("x-ms-encryption-key") String encryptionKey,
                                           @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                           @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);

        @DELETE("{containerName}/{blob}")
        Call<ResponseBody> deleteBlob(@Path("containerName") String containerName,
                                      @Path("blob") String blobName,
                                      @Query("snapshot") String snapshot,
                                      @Query("timeout") Integer timeout,
                                      @Header("x-ms-lease-id") String leaseId,
                                      @Header("x-ms-delete-snapshots") DeleteSnapshotsOptionType deleteSnapshots,
                                      @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                      @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                      @Header("If-Match") String ifMatch,
                                      @Header("If-None-Match") String ifNoneMatch,
                                      @Header("x-ms-version") String version,
                                      @Header("x-ms-client-request-id") String requestId);

        @DELETE("{containerName}")
        Call<ResponseBody> deleteContainer(@Path("containerName") String containerName,
                                           @Query("restype") String restype,
                                           @Query("timeout") Integer timeout,
                                           @Header("x-ms-lease-id") String leaseId,
                                           @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                           @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                           @Header("x-ms-version") String version,
                                           @Header("x-ms-client-request-id") String requestId);
    }
}
