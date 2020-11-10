// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.azure.android.core.http.Response;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.http.interceptor.RequestIdInterceptor;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.implementation.util.ModelHelper;
import com.azure.android.storage.blob.interceptor.MetadataInterceptor;
import com.azure.android.storage.blob.interceptor.NormalizeEtagInterceptor;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobGetTagsResponse;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersResponse;
import com.azure.android.storage.blob.models.BlobSetMetadataResponse;
import com.azure.android.storage.blob.models.BlobSetTagsResponse;
import com.azure.android.storage.blob.models.BlobSetTierResponse;
import com.azure.android.storage.blob.models.BlobTags;
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
import com.azure.android.storage.blob.options.BlobGetPropertiesOptions;
import com.azure.android.storage.blob.options.BlobRawDownloadOptions;
import com.azure.android.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.android.storage.blob.options.BlobSetHttpHeadersOptions;
import com.azure.android.storage.blob.options.BlobSetMetadataOptions;
import com.azure.android.storage.blob.options.ContainerCreateOptions;
import com.azure.android.storage.blob.options.ContainerDeleteOptions;
import com.azure.android.storage.blob.options.ContainerGetPropertiesOptions;

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

    private StorageBlobClient(ServiceClient serviceClient, String serviceVersion) {
        this.serviceClient = serviceClient;
        this.storageBlobServiceClient = new StorageBlobServiceImpl(this.serviceClient, serviceVersion);
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
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param containerName The container name.
     */
    @Nullable
    public Void createContainer(@NonNull String containerName) {
        return this.createContainerWithResponse(new ContainerCreateOptions(containerName)).getValue();
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param options {@link ContainerCreateOptions}
     * @return The response information returned from the server when creating a container.
     */
    @NonNull
    public ContainerCreateResponse createContainerWithResponse(@NonNull ContainerCreateOptions options) {
        Objects.requireNonNull(options);
        return storageBlobServiceClient.createContainerWithRestResponse(options.getContainerName(),
            options.getTimeout(), options.getMetadata(), options.getPublicAccessType(), options.getCancellationToken());
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param containerName The container name.
     */
    @Nullable
    public Void deleteContainer(@NonNull String containerName) {
        return this.deleteContainerWithResponse(new ContainerDeleteOptions(containerName)).getValue();
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param options {@link ContainerDeleteOptions}
     * @return The response information returned from the server when deleting a container.
     */
    @NonNull
    public ContainerDeleteResponse deleteContainerWithResponse(@NonNull ContainerDeleteOptions options) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, true, true, false);
        return storageBlobServiceClient.deleteContainerWithRestResponse(options.getContainerName(),
            options.getTimeout(), options.getRequestConditions(), options.getCancellationToken());
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param containerName The container name.
     * @return The container's properties.
     */
    /* TODO: (gapra) This should probably return a handwrapped type? */
    @NonNull
    public ContainerGetPropertiesHeaders getContainerProperties(@NonNull String containerName) {
        return this.getContainerPropertiesWithResponse(new ContainerGetPropertiesOptions(containerName)).getDeserializedHeaders();
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param options {@link ContainerGetPropertiesOptions}
     * @return The response information returned from the server when getting a container's properties.
     */
    @NonNull
    public ContainerGetPropertiesResponse getContainerPropertiesWithResponse(@NonNull ContainerGetPropertiesOptions options) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, true, false);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        return storageBlobServiceClient.getContainerPropertiesWithResponse(options.getContainerName(),
            options.getTimeout(), requestConditions.getLeaseId(), options.getCancellationToken());
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
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing a list of blobs.
     */
    public Response<BlobsPage> getBlobsInPageWithRestResponse(String pageId,
                                                              String containerName,
                                                              String prefix,
                                                              Integer maxResults,
                                                              List<ListBlobsIncludeItem> include,
                                                              Integer timeout,
                                                              CancellationToken cancellationToken) {
        ContainersListBlobFlatSegmentResponse result
            = this.storageBlobServiceClient.listBlobFlatSegmentWithRestResponse(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
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
     * Returns the blob's metadata and properties. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's metadata and properties
     */
    @NonNull
    public BlobGetPropertiesHeaders getBlobProperties(@NonNull String containerName, @NonNull String blobName) {
        return this.getBlobPropertiesWithResponse(new BlobGetPropertiesOptions(containerName, blobName))
            .getDeserializedHeaders();
    }

    /**
     * Returns the blob's metadata and properties. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>.
     *
     * @param options {@link BlobGetPropertiesOptions}
     * @return The response information returned from the server when getting a blob's properties.
     */
    @NonNull
    public BlobGetPropertiesResponse getBlobPropertiesWithResponse(@NonNull BlobGetPropertiesOptions options) {
        Objects.requireNonNull(options);
        return storageBlobServiceClient.getBlobPropertiesWithRestResponse(options.getContainerName(),
            options.getBlobName(), options.getSnapshot(), options.getTimeout(), options.getRequestConditions(),
            options.getCpkInfo(), options.getCancellationToken());
    }

    /**
     * Changes a blob's HTTP header properties. If only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param headers       {@link BlobHttpHeaders}
     */
    @Nullable
    public Void setBlobHttpHeaders(@NonNull String containerName, @NonNull String blobName,
                                   @Nullable BlobHttpHeaders headers) {
        return this.setBlobHttpHeadersWithResponse(new BlobSetHttpHeadersOptions(containerName, blobName, headers))
            .getValue();
    }

    /**
     * Changes a blob's HTTP header properties. If only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * @param options {@link BlobSetHttpHeadersOptions}
     * @return The response information returned from the server when setting a blob's http headers.
     */
    @NonNull
    public BlobSetHttpHeadersResponse setBlobHttpHeadersWithResponse(@NonNull BlobSetHttpHeadersOptions options) {
        Objects.requireNonNull(options);
        return storageBlobServiceClient.setBlobHttpHeadersWithRestResponse(options.getContainerName(),
            options.getBlobName(), options.getTimeout(), options.getRequestConditions(), options.getHeaders(),
            options.getCancellationToken());
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param metadata      Metadata to associate with the blob.
     */
    @Nullable
    public Void setBlobMetadata(@NonNull String containerName, @NonNull String blobName,
                                @Nullable Map<String, String> metadata) {
        return this.setBlobMetadataWithResponse(new BlobSetMetadataOptions(containerName, blobName, metadata))
            .getValue();
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param options {@link BlobSetMetadataOptions}
     * @return The response information returned from the server when setting a blob's metadata.
     */
    @NonNull
    public BlobSetMetadataResponse setBlobMetadataWithResponse(@NonNull BlobSetMetadataOptions options) {
        return storageBlobServiceClient.setBlobMetadataWithRestResponse(options.getContainerName(),
            options.getBlobName(), options.getTimeout(), options.getRequestConditions(), options.getMetadata(),
            options.getCpkInfo(), options.getCancellationToken());
    }

    /* TODO: (gapra) Should we remove everything related to PageBlobs here? */
    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tier          The access tier.
     */
    @Nullable
    public Void setBlobAccessTier(@NonNull String containerName, @NonNull String blobName, @Nullable AccessTier tier) {
        return this.setBlobAccessTierWithResponse(new BlobSetAccessTierOptions(containerName, blobName, tier))
            .getValue();
    }

    /**
     * Sets the tier on a blob. The operation is allowed on a page blob in a premium storage account or a block blob in
     * a blob storage or GPV2 account. A premium page blob's tier determines the allowed size, IOPS, and bandwidth of
     * the blob. A block blob's tier determines the Hot/Cool/Archive storage type. This does not update the blob's
     * etag.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tier">Azure Docs</a></p>
     *
     * @param options {@link BlobSetAccessTierOptions}
     * @return The response information returned from the server when setting a blob's access tier.
     */
    @NonNull
    public BlobSetTierResponse setBlobAccessTierWithResponse(@NonNull BlobSetAccessTierOptions options) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, true, true);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        return storageBlobServiceClient.setBlobTierWithRestResponse(options.getContainerName(), options.getBlobName(),
            options.getAccessTier(), options.getSnapshot(), null /*TODO: (gapra) VersionId?*/ , options.getTimeout(),
            options.getRehydratePriority(), requestConditions.getLeaseId(), requestConditions.getTagsConditions(),
            options.getCancellationToken());
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The response containing the blob's bytes.
     */
    @NonNull
    public ResponseBody rawDownload(@NonNull String containerName,
                                    @NonNull String blobName) {
        return this.rawDownloadWithResponse(new BlobRawDownloadOptions(containerName, blobName)).getValue();
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
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobRawDownloadOptions}
     * @return The response information returned from the server when downloading a blob.
     */
    @NonNull
    public BlobDownloadResponse rawDownloadWithResponse(@NonNull BlobRawDownloadOptions options) {
        Objects.requireNonNull(options);
        BlobRange range = options.getRange() == null ? new BlobRange(0) : options.getRange();

        return storageBlobServiceClient.downloadWithRestResponse(options.getContainerName(), options.getBlobName(),
            options.getSnapshot(), options.getTimeout(), range.toHeaderValue(), options.isRetrieveContentRangeMd5(),
            options.isRetrieveContentRangeCrc64(), options.getRequestConditions(), options.getCpkInfo(),
            options.getCancellationToken());
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
     * @param computeMd5        Whether or not the library should calculate the md5 and send it for the service to verify.
     * @param timeout           The timeout parameter is expressed in seconds. For more information,
     *                          see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId           If specified, the staging only succeeds if the resource's lease is active and matches this ID.
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
                                                                   Boolean computeMd5,
                                                                   Integer timeout,
                                                                   String leaseId,
                                                                   CpkInfo cpkInfo,
                                                                   CancellationToken cancellationToken) {
        return this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5,
            contentCrc64,
            computeMd5,
            timeout,
            leaseId,
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
    public Void deleteBlob(String containerName,
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
     * @param deleteSnapshots   Required if the blob has associated snapshots. Specify one of the following two
     *                          options: include: Delete the base blob and all of its snapshots. only: Delete only the blob's snapshots and not the blob itself. Possible values include: 'include', 'only'.
     * @param requestConditions {@link BlobRequestConditions}
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing the details of the delete operation.
     */
    public BlobDeleteResponse deleteBlobWithRestResponse(String containerName,
                                                         String blobName,
                                                         String snapshot,
                                                         Integer timeout,
                                                         DeleteSnapshotsOptionType deleteSnapshots,
                                                         BlobRequestConditions requestConditions,
                                                         CancellationToken cancellationToken) {
        return storageBlobServiceClient.deleteBlobWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            deleteSnapshots,
            requestConditions,
            cancellationToken);
    }

    /**
     * Gets tags associated with a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's tags.
     */
    public Map<String, String> getBlobTags(String containerName,
                                           String blobName) {
        BlobTags response = this.storageBlobServiceClient.getTags(containerName,
            blobName);
        return ModelHelper.populateBlobTags(response);
    }

    /**
     * Gets tags associated with a blob.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the
     *                          blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing the blob's tags.
     */
    public Response<Map<String, String>> getBlobTagsWithRestResponse(String containerName,
                                                                     String blobName,
                                                                     String snapshot,
                                                                     Integer timeout,
                                                                     String tagsConditions,
                                                                     CancellationToken cancellationToken) {
        BlobGetTagsResponse response = this.storageBlobServiceClient.getTagsWithRestResponse(containerName,
            blobName,
            snapshot,
            null, /* TODO (gapra) : Add in support when we set version to STG73 */
            timeout,
            tagsConditions,
            cancellationToken);

        return new Response<>(null,
            response.getStatusCode(),
            response.getHeaders(),
            ModelHelper.populateBlobTags(response.getValue()));
    }

    /**
     * Changes a blob's tags. The specified tags in this method will replace existing tags. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tags          Tags to associate with the blob.
     */
    public Void setBlobTags(String containerName,
                            String blobName,
                            Map<String, String> tags) {
        return storageBlobServiceClient.setBlobTags(containerName, blobName, tags);
    }

    /**
     * Changes a blob's tags. The specified tags in this method will replace existing tags. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param tagsConditions            Specifies a SQL query to apply to the blob's tags.
     * @param tags              Tags to associate with the blob.
     * @param cancellationToken The token to request cancellation.
     */
    public BlobSetTagsResponse setBlobTagsWithResponse(String containerName,
                                                       String blobName,
                                                       Integer timeout,
                                                       String tagsConditions, /*TODO: Should this be BlobRequestConditions? */
                                                       Map<String, String> tags,
                                                       CancellationToken cancellationToken) {
        return storageBlobServiceClient.setBlobTagsWithRestResponse(containerName,
            blobName,
            timeout,
            null, // TODO: Add back with versioning support
            tagsConditions,
            tags,
            cancellationToken);
    }

    /**
     * Builder for {@link StorageBlobClient}.
     * A builder to configure and build a {@link StorageBlobClient}.
     */
    public static class Builder {
        private final ServiceClient.Builder serviceClientBuilder;
        private BlobServiceVersion serviceVersion;

        /**
         * Creates a {@link Builder}.
         */
        public Builder() {
            this(new ServiceClient.Builder());
            addStandardInterceptors();
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
            addStandardInterceptors();
        }

        private void addStandardInterceptors() {
            this.serviceClientBuilder
                .addInterceptor(new RequestIdInterceptor())
                .addInterceptor(new AddDateInterceptor())
                .addInterceptor(new MetadataInterceptor())
                .addInterceptor(new NormalizeEtagInterceptor());
            //.addInterceptor(new ResponseHeadersValidationInterceptor()); // TODO: Uncomment when we add a request id interceptor
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
         * Sets the service version for the {@link StorageBlobClient}.
         *
         * @param serviceVersion {@link BlobServiceVersion}
         * @return An updated {@link StorageBlobClient.Builder} with the provided blob service version set.
         */
        public StorageBlobClient.Builder setServiceVersion(BlobServiceVersion serviceVersion) {
            this.serviceVersion = serviceVersion;
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
            BlobServiceVersion version = this.serviceVersion == null ? BlobServiceVersion.getLatest()
                : this.serviceVersion;
            StorageBlobClient client = new StorageBlobClient(this.serviceClientBuilder.build(), version.getVersion());
            return client;
        }

        private Builder(final StorageBlobClient storageBlobClient) {
            this(storageBlobClient.serviceClient.newBuilder());
        }
    }
}
