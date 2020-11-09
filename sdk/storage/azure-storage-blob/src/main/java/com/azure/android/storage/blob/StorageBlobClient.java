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
import com.azure.android.storage.blob.models.RehydratePriority;
import com.azure.android.storage.blob.options.ContainerCreateOptions;

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
            options.getTimeout(),
            options.getMetadata(),
            options.getPublicAccessType(),
            options.getCancellationToken());
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
     * Reads the blob's metadata and properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's metadata and properties
     */
    public BlobGetPropertiesHeaders getBlobProperties(String containerName,
                                                      String blobName) {
        return storageBlobServiceClient.getBlobProperties(containerName, blobName);
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
     * @param blobRequestConditions Object that contains values which will restrict the successful operation of a
     *                              variety of requests to the conditions present. These conditions are entirely
     *                              optional.
     * @param cpkInfo               Additional parameters for the operation.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                       String blobName,
                                                                       String snapshot,
                                                                       Integer timeout,
                                                                       BlobRequestConditions blobRequestConditions,
                                                                       CpkInfo cpkInfo,
                                                                       CancellationToken cancellationToken) {
        return storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            blobRequestConditions,
            cpkInfo,
            cancellationToken);
    }

    /**
     * Changes a blob's HTTP header properties. If only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param headers       {@link BlobHttpHeaders}
     */
    public Void setBlobHttpHeaders(String containerName,
                                   String blobName,
                                   BlobHttpHeaders headers) {
        return storageBlobServiceClient.setBlobHttpHeaders(containerName, blobName, headers);
    }

    /**
     * Changes a blob's HTTP header properties. If only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param headers           {@link BlobHttpHeaders}
     * @param cancellationToken The token to request cancellation.
     * @return The response object.
     */
    public BlobSetHttpHeadersResponse setBlobHttpHeadersWithResponse(String containerName,
                                                                     String blobName,
                                                                     Integer timeout,
                                                                     BlobRequestConditions requestConditions,
                                                                     BlobHttpHeaders headers,
                                                                     CancellationToken cancellationToken) {
        return storageBlobServiceClient.setBlobHttpHeadersWithRestResponse(containerName,
            blobName,
            timeout,
            requestConditions,
            headers,
            cancellationToken);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param metadata      Metadata to associate with the blob.
     */
    public Void setBlobMetadata(String containerName,
                                String blobName,
                                Map<String, String> metadata) {
        return storageBlobServiceClient.setBlobMetadata(containerName, blobName, metadata);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param metadata          Metadata to associate with the blob.
     * @param cpkInfo           Additional parameters for the operation.
     * @param cancellationToken The token to request cancellation.
     */
    public BlobSetMetadataResponse setBlobMetadataWithResponse(String containerName,
                                                               String blobName,
                                                               Integer timeout,
                                                               BlobRequestConditions requestConditions,
                                                               Map<String, String> metadata,
                                                               CpkInfo cpkInfo,
                                                               CancellationToken cancellationToken) {
        return storageBlobServiceClient.setBlobMetadataWithRestResponse(containerName,
            blobName,
            timeout,
            requestConditions,
            metadata,
            cpkInfo,
            cancellationToken);
    }

    /**
     * Sets the blob's tier.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tier          The access tier.
     */
    public Void setBlobTier(String containerName,
                            String blobName,
                            AccessTier tier) {
        return storageBlobServiceClient.setBlobTier(containerName, blobName, tier);
    }

    /**
     * Sets the blob's tier.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param tier              The access tier.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies
     *                          the blob snapshot to retrieve. For more information on working with blob snapshots,
     *                          see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param rehydratePriority The rehydrate priority.
     * @return The response information returned from the server when setting a blob's access tier.
     */
    public BlobSetTierResponse setBlobTierWithRestResponse(String containerName,
                                                           String blobName,
                                                           AccessTier tier,
                                                           String snapshot,
                                                           Integer timeout,
                                                           RehydratePriority rehydratePriority,
                                                           String leaseId,
                                                           String tagsConditions,
                                                           CancellationToken cancellationToken) {

        return storageBlobServiceClient.setBlobTierWithRestResponse(containerName,
            blobName,
            tier,
            snapshot,
            null,  /* TODO: (gapra) Add version id when there is support for STG73 */
            timeout,
            rehydratePriority,
            leaseId,
            tagsConditions,
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
     * @param blobRequestConditions Object that contains values which will restrict the successful operation of a
     *                              variety of requests to the conditions present. These conditions are entirely
     *                              optional.
     * @param cancellationToken     The token to request cancellation.
     * @return The response information returned from the server when getting a container's properties.
     */
    public ContainerGetPropertiesResponse getContainerPropertiesWithRestResponse(String containerName,
                                                                                 Integer timeout,
                                                                                 BlobRequestConditions blobRequestConditions,
                                                                                 CancellationToken cancellationToken) {
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.getContainerPropertiesWithResponse(containerName,
            timeout,
            blobRequestConditions.getLeaseId(),
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
     * `
     *
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
                                                            CpkInfo cpkInfo,
                                                            CancellationToken cancellationToken) {
        range = range == null ? new BlobRange(0) : range;
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.downloadWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            range.toHeaderValue(),
            getRangeContentMd5,
            getRangeContentCrc64,
            blobRequestConditions,
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
     * Deletes a container.
     *
     * @param containerName The container name.
     */
    public Void deleteContainer(String containerName) {
        return storageBlobServiceClient.deleteContainer(containerName);
    }

    /**
     * Deletes a container
     *
     * @param containerName     The container name.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param cancellationToken The token to request cancellation.
     * @return A response object containing the details of the delete operation.
     */
    public ContainerDeleteResponse deleteContainerWithRestResponse(String containerName,
                                                                   Integer timeout,
                                                                   BlobRequestConditions requestConditions,
                                                                   CancellationToken cancellationToken) {
        return storageBlobServiceClient.deleteContainerWithRestResponse(containerName,
            timeout,
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
