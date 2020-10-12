// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import android.content.Context;
import android.net.Uri;

import com.azure.android.core.http.Response;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.ContainerCreateResponse;
import com.azure.android.storage.blob.models.ContainerDeleteResponse;
import com.azure.android.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.android.storage.blob.models.ContainerGetPropertiesResponse;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.android.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.models.PublicAccessType;

import org.threeten.bp.OffsetDateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.ResponseBody;

/**
 * Client for Storage Blob service.
 *
 * <p>
 * This client is instantiated through {@link StorageBlobClient.Builder}.
 */
public class StorageBlobClient {
    private final ServiceClient serviceClient;
    private final StorageBlobServiceImpl storageBlobServiceClient;

    private StorageBlobClient(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
        this.storageBlobServiceClient = new StorageBlobServiceImpl(this.serviceClient);
    }

    /**
     * Creates a new {@link Builder} with initial configuration copied from this {@link StorageBlobClient}
     *
     * @return A new {@link Builder}.
     */
    public StorageBlobClient.Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Gets the blob service base URL.
     *
     * @return The blob service base URL.
     */
    public String getBlobServiceUrl() {
        return this.serviceClient.getBaseUrl();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails.
     *
     * @param containerName The container name.

     */
    public Void createContainer(String containerName) {
        return storageBlobServiceClient.createContainer(containerName);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails.
     *
     * @param containerName         The container name.
     * @param timeout               The timeout parameter is expressed in seconds. For more information, see
     *                              &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param metadata              Metadata to associate with the container.
     * @param publicAccessType      Specifies how the data in this container is available to the public. See the
     *                              x-ms-blob-public-access header in the Azure Docs for more information. Pass null
     *                              for no public access.
     * @param version               Specifies the version of the operation to use for this request.
     * @param requestId             Provides a client-generated, opaque value with a 1 KB character limit that is
     *                              recorded in the analytics logs when storage analytics logging is enabled.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when creating a container.
     */
    public ContainerCreateResponse createContainerWithRestResponse(String containerName,
                                                                   Integer timeout,
                                                                   Map<String, String> metadata,
                                                                   PublicAccessType publicAccessType,
                                                                   String version,
                                                                   String requestId,
                                                                   CancellationToken cancellationToken) {

        return storageBlobServiceClient.createContainerWithRestResponse(containerName,
            timeout,
            metadata,
            publicAccessType,
            version,
            requestId,
            cancellationToken);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options       The page options.
     * @return A list of blobs.
     */
    public BlobsPage getBlobsInPage(String pageId,
                                    String containerName,
                                    ListBlobsOptions options) {
        ListBlobsFlatSegmentResponse result = this.storageBlobServiceClient.listBlobFlatSegment(pageId,
            containerName, options);

        final List<BlobItem> list;
        if (result.getSegment() != null
            && result.getSegment().getBlobItems() != null) {
            list = result.getSegment().getBlobItems();
        } else {
            list = new ArrayList<>(0);
        }
        return new BlobsPage(list, pageId, result.getNextMarker());
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId            Identifies the portion of the list to be returned.
     * @param containerName     The container name.
     * @param prefix            Filters the results to return only blobs whose name begins with the specified prefix.
     * @param maxResults        Specifies the maximum number of blobs to return.
     * @param include           Include this parameter to specify one or more datasets to include in the response.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is recorded in
     *                          the analytics logs when storage analytics logging is enabled.
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing a list of blobs.
     */
    public Response<BlobsPage> getBlobsInPageWithRestResponse(String pageId,
                                                              String containerName,
                                                              String prefix,
                                                              Integer maxResults,
                                                              List<ListBlobsIncludeItem> include,
                                                              Integer timeout,
                                                              String requestId,
                                                              CancellationToken cancellationToken) {
        ContainersListBlobFlatSegmentResponse result
            = this.storageBlobServiceClient.listBlobFlatSegmentWithRestResponse(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            cancellationToken);
        final List<BlobItem> list;
        if (result.getValue().getSegment() != null
            && result.getValue().getSegment().getBlobItems() != null) {
            list = result.getValue().getSegment().getBlobItems();
        } else {
            list = new ArrayList<>(0);
        }
        BlobsPage blobsPage = new BlobsPage(list, pageId, result.getValue().getNextMarker());

        return new Response<>(null,
            result.getStatusCode(),
            result.getHeaders(),
            blobsPage);
    }

    /**
     * Reads the blob's metadata and properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's metadata and properties
     */
    public BlobGetPropertiesHeaders getBlobProperties(String containerName,
                                                      String blobName) {
        return storageBlobServiceClient.getBlobProperties(containerName,  blobName);
    }

    /**
     * Reads a blob's metadata and properties.
     *
     * @param containerName         The container name.
     * @param blobName              The blob name.
     * @param snapshot              The snapshot parameter is an opaque DateTime value that, when present, specifies
     *                              the blob snapshot to retrieve. For more information on working with blob snapshots,
     *                              see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout               The timeout parameter is expressed in seconds. For more information, see
     *                              &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version               Specifies the version of the operation to use for this request.
     * @param blobRequestConditions Object that contains values which will restrict the successful operation of a
     *                              variety of requests to the conditions present. These conditions are entirely
     *                              optional.
     * @param requestId             Provides a client-generated, opaque value with a 1 KB character limit that is
     *                              recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo               Additional parameters for the operation.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                       String blobName,
                                                                       String snapshot,
                                                                       Integer timeout,
                                                                       String version,
                                                                       BlobRequestConditions blobRequestConditions,
                                                                       String requestId,
                                                                       CpkInfo cpkInfo,
                                                                       CancellationToken cancellationToken) {
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            blobRequestConditions.getLeaseId(),
            requestId,
            cpkInfo,
            cancellationToken);
    }

    /**
     * Gets the container's properties.
     *
     * @param containerName The container name.
     * @return The container's properties
     */
    public ContainerGetPropertiesHeaders getContainerProperties(String containerName) {
        return storageBlobServiceClient.getContainerProperties(containerName);
    }

    /**
     * Gets the container's properties.
     *
     * @param containerName         The container name.
     * @param timeout               The timeout parameter is expressed in seconds. For more information, see
     *                              &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version               Specifies the version of the operation to use for this request.
     * @param blobRequestConditions Object that contains values which will restrict the successful operation of a
     *                              variety of requests to the conditions present. These conditions are entirely
     *                              optional.
     * @param requestId             Provides a client-generated, opaque value with a 1 KB character limit that is
     *                              recorded in the analytics logs when storage analytics logging is enabled.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when downloading a blob.
     */
    public ContainerGetPropertiesResponse getContainerPropertiesWithRestResponse(String containerName,
                                                                                 Integer timeout,
                                                                                 String version,
                                                                                 BlobRequestConditions blobRequestConditions,
                                                                                 String requestId,
                                                                                 CancellationToken cancellationToken) {
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.getContainerPropertiesWithResponse(containerName,
            timeout,
            version,
            blobRequestConditions.getLeaseId(),
            requestId,
            cancellationToken);
    }

    /**
     * Reads the entire blob.
     *
     * <p>
     * This method will execute a raw HTTP GET in order to download a single blob to the destination.
     * It is **STRONGLY** recommended that you use the {@link StorageBlobAsyncClient#download(Context, String, String, File)}
     * or {@link StorageBlobAsyncClient#download(Context, String, String, Uri)} method instead - that method will
     * manage the transfer in the face of changing network conditions, and is able to transfer multiple
     * blocks in parallel.
     *`
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The response containing the blob's bytes.
     */
    public ResponseBody rawDownload(String containerName,
                                    String blobName) {
        return storageBlobServiceClient.download(containerName,
            blobName);
    }

    /**
     * Reads a range of bytes from a blob.
     *
     * <p>
     * This method will execute a raw HTTP GET in order to download a single blob to the destination.
     * It is **STRONGLY** recommended that you use the {@link StorageBlobAsyncClient#download(Context, String, String, File)}
     * or {@link StorageBlobAsyncClient#download(Context, String, String, Uri)} method instead - that method will
     * manage the transfer in the face of changing network conditions, and is able to transfer multiple
     * blocks in parallel.
     *
     * @param containerName         The container name.
     * @param blobName              The blob name.
     * @param snapshot              The snapshot parameter is an opaque DateTime value that, when present, specifies
     *                              the blob snapshot to retrieve. For more information on working with blob snapshots,
     *                              see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout               The timeout parameter is expressed in seconds. For more information, see
     *                              &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param range                 Return only the bytes of the blob in the specified range.
     * @param blobRequestConditions Object that contains values which will restrict the successful operation of a
     *                              variety of requests to the conditions present. These conditions are entirely
     *                              optional.
     * @param getRangeContentMd5    When set to true and specified together with the Range, the service returns the
     *                              MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param getRangeContentCrc64  When set to true and specified together with the Range, the service returns the
     *                              CRC64 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param version               Specifies the version of the operation to use for this request.
     * @param requestId             Provides a client-generated, opaque value with a 1 KB character limit that is
     *                              recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo               Additional parameters for the operation.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobDownloadResponse rawDownloadWithRestResponse(String containerName,
                                                            String blobName,
                                                            String snapshot,
                                                            Integer timeout,
                                                            BlobRange range,
                                                            BlobRequestConditions blobRequestConditions,
                                                            Boolean getRangeContentMd5,
                                                            Boolean getRangeContentCrc64,
                                                            String version,
                                                            String requestId,
                                                            CpkInfo cpkInfo,
                                                            CancellationToken cancellationToken) {
        range = range == null ? new BlobRange(0) : range;
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.downloadWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            range.toHeaderValue(),
            blobRequestConditions.getLeaseId(),
            getRangeContentMd5,
            getRangeContentCrc64,
            blobRequestConditions.getIfModifiedSince(),
            blobRequestConditions.getIfUnmodifiedSince(),
            blobRequestConditions.getIfMatch(),
            blobRequestConditions.getIfNoneMatch(),
            version,
            requestId,
            cpkInfo,
            cancellationToken);
    }

    /**
     * Creates a new block to be committed as part of a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param base64BlockId A valid Base64 string value that identifies the block. Prior to encoding, the string must
     *                      be less than or equal to 64 bytes in size. For a given blob, the length of the value specified
     *                      for the base64BlockId parameter must be the same size for each block.
     * @param blockContent  The block content in bytes.
     * @param contentMd5    The transactional MD5 for the body, to be validated by the service.
     */
    public Void stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] blockContent,
                           byte[] contentMd5) {
        return this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5);
    }

    /**
     * Creates a new block to be committed as part of a blob.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param base64BlockId     A valid Base64 string value that identifies the block. Prior to encoding, the string must
     *                          be less than or equal to 64 bytes in size. For a given blob, the length of the value specified
     *                          for the base64BlockId parameter must be the same size for each block.
     * @param blockContent      The block content in bytes.
     * @param contentMd5        The transactional MD5 for the block content, to be validated by the service.
     * @param contentCrc64      Specify the transactional crc64 for the block content, to be validated by the service.
     * @param timeout           The timeout parameter is expressed in seconds. For more information,
     *     see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId           If specified, the staging only succeeds if the resource's lease is active and matches this ID.
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is recorded.
     *                          in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo           Additional parameters for the operation.
     * @param cancellationToken The token to request cancellation.
     * @return The response object.
     */
    public BlockBlobsStageBlockResponse stageBlockWithRestResponse(String containerName,
                                                                   String blobName,
                                                                   String base64BlockId,
                                                                   byte[] blockContent,
                                                                   byte[] contentMd5,
                                                                   byte[] contentCrc64,
                                                                   Integer timeout,
                                                                   String leaseId,
                                                                   String requestId,
                                                                   CpkInfo cpkInfo,
                                                                   CancellationToken cancellationToken) {
        return this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5,
            contentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            cancellationToken);
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobClient#stageBlock(String, String, String, byte[], byte[])} operation. You can call commit Block List
     * to update a blob by uploading only those blocks that have changed, then committing the new and existing blocks together.
     * You can do this by specifying whether to commit a block from the committed block list or from the uncommitted block list,
     * or to commit the most recently uploaded version of the block, whichever list it may belong to.
     *
     * @param containerName  The container name.
     * @param blobName       The blob name.
     * @param base64BlockIds The block IDs.
     * @param overwrite      Indicate whether to overwrite the block list if already exists.
     * @return The properties of the block blob
     */
    public BlockBlobItem commitBlockList(String containerName,
                                         String blobName,
                                         List<String> base64BlockIds,
                                         boolean overwrite) {
        return this.storageBlobServiceClient.commitBlockList(containerName,
            blobName,
            base64BlockIds,
            overwrite);
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobClient#stageBlock(String, String, String, byte[], byte[])} operation. You can call commit Block List
     * to update a blob by uploading only those blocks that have changed, then committing the new and existing blocks together.
     * You can do this by specifying whether to commit a block from the committed block list or from the uncommitted block list,
     * or to commit the most recently uploaded version of the block, whichever list it may belong to.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param base64BlockIds    The block IDs.
     * @param contentMD5        Specify the transactional md5 for the body, to be validated by the service.
     * @param contentCrc64      Specify the transactional crc64 for the body, to be validated by the service.
     * @param timeout           The timeout parameter is expressed in seconds. For more information,
     *                          see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param blobHttpHeaders   Additional Http headers for this operation.
     * @param metadata          Specifies a user-defined name-value pair associated with the blob.
     * @param requestConditions {@link BlobRequestConditions}.
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is recorded
     *                          in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo           Additional parameters for the operation.
     * @param tier              Indicates the tier to be set on the blob.
     * @param cancellationToken The token to request cancellation.
     * @return The response object.
     */
    public BlockBlobsCommitBlockListResponse commitBlockListWithRestResponse(String containerName,
                                                                             String blobName,
                                                                             List<String> base64BlockIds,
                                                                             byte[] contentMD5,
                                                                             byte[] contentCrc64,
                                                                             Integer timeout,
                                                                             BlobHttpHeaders blobHttpHeaders,
                                                                             Map<String, String> metadata,
                                                                             BlobRequestConditions requestConditions,
                                                                             String requestId,
                                                                             CpkInfo cpkInfo,
                                                                             AccessTier tier,
                                                                             CancellationToken cancellationToken) {
        return this.storageBlobServiceClient.commitBlockListWithRestResponse(containerName,
            blobName,
            base64BlockIds,
            contentMD5,
            contentCrc64,
            timeout,
            blobHttpHeaders,
            metadata,
            requestConditions,
            requestId,
            cpkInfo,
            tier,
            cancellationToken);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    Void deleteBlob(String containerName,
                    String blobName) {
        return storageBlobServiceClient.deleteBlob(containerName,
            blobName);
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
     * @param cancellationToken The token to request cancellation.
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
        return storageBlobServiceClient.deleteBlobWithRestResponse(containerName,
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
            cancellationToken);
    }

    /**
     * Deletes a container.
     *
     * @param containerName The container name.
     */
    Void deleteContainer(String containerName) {
        return storageBlobServiceClient.deleteContainer(containerName);
    }

    /**
     * Deletes a container
     *
     * @param containerName     The container name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param requestId         Provides a client-generated, opaque value with a 1 KB character limit that is
     *                          recorded in the analytics logs when storage analytics logging is enabled.
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing the details of the delete operation.
     */
    ContainerDeleteResponse deleteContainerWithRestResponse(String containerName,
                                                            Integer timeout,
                                                            String version,
                                                            BlobRequestConditions requestConditions,
                                                            String requestId,
                                                            CancellationToken cancellationToken) {
        return storageBlobServiceClient.deleteContainerWithRestResponse(containerName,
            timeout,
            version,
            requestConditions,
            requestId,
            cancellationToken);
    }

    /**
     * Builder for {@link StorageBlobClient}.
     * A builder to configure and build a {@link StorageBlobClient}.
     */
    public static class Builder {
        private final ServiceClient.Builder serviceClientBuilder;

        /**
         * Creates a {@link Builder}.
         */
        public Builder() {
            this(new ServiceClient.Builder());
            this.serviceClientBuilder
                .addInterceptor(new AddDateInterceptor());
        }

        /**
         * Creates a {@link Builder} that uses the provided {@link com.azure.android.core.http.ServiceClient.Builder}
         * to build a {@link ServiceClient} for the {@link StorageBlobClient}.
         *
         * <p>
         * The builder produced {@link ServiceClient} is used by the {@link StorageBlobClient} to make Rest API calls.
         * Multiple {@link StorageBlobClient} instances can share the same {@link ServiceClient} instance, for e.g.
         * when a new {@link StorageBlobClient} is created from an existing {@link StorageBlobClient} through
         * {@link StorageBlobClient#newBuilder()} ()} then both shares the same {@link ServiceClient}.
         * The {@link ServiceClient} composes HttpClient, HTTP settings (such as connection timeout, interceptors)
         * and Retrofit for Rest calls.
         *
         * @param serviceClientBuilder The {@link com.azure.android.core.http.ServiceClient.Builder}.
         */
        public Builder(ServiceClient.Builder serviceClientBuilder) {
            this.serviceClientBuilder
                = Objects.requireNonNull(serviceClientBuilder, "serviceClientBuilder cannot be null.");
        }

        /**
         * Sets the base URL for the {@link StorageBlobClient}.
         *
         * @param blobServiceUrl The blob service base URL.
         * @return An updated {@link Builder} with the provided blob service URL set.
         */
        public Builder setBlobServiceUrl(String blobServiceUrl) {
            Objects.requireNonNull(blobServiceUrl, "blobServiceUrl cannot be null.");
            this.serviceClientBuilder.setBaseUrl(blobServiceUrl);
            return this;
        }

        /**
         * Sets an interceptor used to authenticate the blob service request.
         *
         * @param credentialInterceptor The credential interceptor.
         * @return An updated {@link Builder} with the provided credentials interceptor set.
         */
        public Builder setCredentialInterceptor(Interceptor credentialInterceptor) {
            this.serviceClientBuilder.setCredentialsInterceptor(credentialInterceptor);
            return this;
        }

        /**
         * Builds a {@link StorageBlobClient} based on this {@link Builder}'s configuration.
         *
         * @return A {@link StorageBlobClient}.
         */
        public StorageBlobClient build() {
            StorageBlobClient client = new StorageBlobClient(this.serviceClientBuilder.build());
            return client;
        }

        private Builder(final StorageBlobClient storageBlobClient) {
            this(storageBlobClient.serviceClient.newBuilder());
        }
    }
}
