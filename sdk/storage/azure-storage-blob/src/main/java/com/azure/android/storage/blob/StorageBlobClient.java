// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import android.content.Context;
import android.net.Uri;

import com.azure.android.core.http.Response;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.models.BlobDeleteOptions;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadOptions;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobProperties;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.BlockLookupList;
import com.azure.android.storage.blob.models.CommitBlockListOptions;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.GetBlobPropertiesOptions;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.models.StageBlockOptions;
import com.azure.android.storage.blob.models.StageBlockResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @return A list of blobs.
     */
    public BlobsPage getBlobsInPage(String pageId,
                                         String containerName) {

        ContainersListBlobFlatSegmentResponse containersListBlobFlatSegmentResponse
            = this.storageBlobServiceClient.listBlobFlatSegmentWithRestResponse(containerName,
            ClientUtil.toImplOptions(pageId, new ListBlobsOptions()));
        List<BlobItem> list = containersListBlobFlatSegmentResponse.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : containersListBlobFlatSegmentResponse.getValue().getSegment().getBlobItems();
        return new BlobsPage(list, pageId, containersListBlobFlatSegmentResponse.getValue().getNextMarker());
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId            Identifies the portion of the list to be returned.
     * @param containerName     The container name.
     * @param options           The optional parameter.
     * @return A response object containing a list of blobs.
     */
    public Response<BlobsPage> getBlobsInPageWithRestResponse(String pageId,
                                                                  String containerName,
                                                                  ListBlobsOptions options) {
        ContainersListBlobFlatSegmentResponse containersListBlobFlatSegmentResponse
            = this.storageBlobServiceClient.listBlobFlatSegmentWithRestResponse(containerName,
            ClientUtil.toImplOptions(pageId, options));

        List<BlobItem> list = containersListBlobFlatSegmentResponse.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : containersListBlobFlatSegmentResponse.getValue().getSegment().getBlobItems();

        BlobsPage blobsPage = new BlobsPage(list, pageId, containersListBlobFlatSegmentResponse.getValue().getNextMarker());

        return new Response<>(null,
            containersListBlobFlatSegmentResponse.getStatusCode(),
            containersListBlobFlatSegmentResponse.getHeaders(),
            blobsPage);
    }

    /**
     * Reads the blob's metadata and properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return The blob's metadata and properties
     */
    public BlobProperties getBlobProperties(String containerName,
                                            String blobName) {
        final BlobGetPropertiesResponse blobGetPropertiesResponse
            = storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            new com.azure.android.storage.blob.implementation.models.GetBlobPropertiesOptions());
        return ClientUtil.buildBlobProperties(blobGetPropertiesResponse.getDeserializedHeaders());
    }

    /**
     * Reads a blob's metadata and properties.
     *
     * @param containerName         The container name.
     * @param blobName              The blob name.
     * @param options               The optional parameter.
     * @return The response information returned from the server when downloading a blob.
     */
    public Response<BlobProperties> getBlobPropertiesWithRestResponse(String containerName,
                                                            String blobName,
                                                            GetBlobPropertiesOptions options) {
        final BlobGetPropertiesResponse blobGetPropertiesResponse
            = storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            ClientUtil.toImplOptions(options));

        return new Response<>(null,
            blobGetPropertiesResponse.getStatusCode(),
            blobGetPropertiesResponse.getHeaders(),
            ClientUtil.buildBlobProperties(blobGetPropertiesResponse.getDeserializedHeaders()));
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
        com.azure.android.storage.blob.implementation.models.BlobDownloadOptions implOptions = ClientUtil.toImplOptions(new BlobDownloadOptions());
        implOptions.setRange(new BlobRange(0).toString());

        BlobDownloadResponse blobDownloadResponse = storageBlobServiceClient.downloadWithRestResponse(containerName,
            blobName,
            implOptions);
        return blobDownloadResponse.getValue();
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
     * @param options               The optional parameter.
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobDownloadResponse rawDownloadWithRestResponse(String containerName,
                                                            String blobName,
                                                            BlobDownloadOptions options) {
        String range = options.getRange() == null ? new BlobRange(0).toString() : options.getRange().toString();
        com.azure.android.storage.blob.implementation.models.BlobDownloadOptions implOptions = ClientUtil.toImplOptions(options);
        implOptions.setRange(range);

        return storageBlobServiceClient.downloadWithRestResponse(containerName,
            blobName,
            implOptions);
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
    public StageBlockResult stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] blockContent,
                           byte[] contentMd5) {
        BlockBlobsStageBlockResponse blockBlobsStageBlockResponse = this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent.length,
            blockContent,
            ClientUtil.toImplOptions(new StageBlockOptions()).setTransactionalContentMD5(contentMd5));

        return new StageBlockResult(
            blockBlobsStageBlockResponse.getDeserializedHeaders().getContentMD5(),
            blockBlobsStageBlockResponse.getDeserializedHeaders().getDateProperty(),
            blockBlobsStageBlockResponse.getDeserializedHeaders().getXMsContentCrc64(),
            blockBlobsStageBlockResponse.getDeserializedHeaders().isServerEncrypted(),
            blockBlobsStageBlockResponse.getDeserializedHeaders().getEncryptionKeySha256());
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
     * @param options           The optional parameter.
     * @return The response object.
     */
    public Response<StageBlockResult> stageBlockWithRestResponse(String containerName,
                                                                 String blobName,
                                                                 String base64BlockId,
                                                                 byte[] blockContent,
                                                                 byte[] contentMd5,
                                                                 StageBlockOptions options) {
        BlockBlobsStageBlockResponse blockBlobsStageBlockResponse = this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent.length,
            blockContent,
            ClientUtil.toImplOptions(options).setTransactionalContentMD5(contentMd5));

        return new Response<>(null,
            blockBlobsStageBlockResponse.getStatusCode(),
            blockBlobsStageBlockResponse.getHeaders(),
            new StageBlockResult(
                blockBlobsStageBlockResponse.getDeserializedHeaders().getContentMD5(),
                blockBlobsStageBlockResponse.getDeserializedHeaders().getDateProperty(),
                blockBlobsStageBlockResponse.getDeserializedHeaders().getXMsContentCrc64(),
                blockBlobsStageBlockResponse.getDeserializedHeaders().isServerEncrypted(),
                blockBlobsStageBlockResponse.getDeserializedHeaders().getEncryptionKeySha256()));
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
     * @return The properties of the block blob
     */
    public BlockBlobItem commitBlockList(String containerName,
                                         String blobName,
                                         List<String> base64BlockIds) {
        BlockBlobsCommitBlockListResponse commitBlockListResponse = this.storageBlobServiceClient.commitBlockListWithRestResponse(containerName,
            blobName,
            new BlockLookupList().setCommitted(base64BlockIds),
            new com.azure.android.storage.blob.implementation.models.CommitBlockListOptions());

        return ClientUtil.buildBlockBlobItem(commitBlockListResponse.getDeserializedHeaders());
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
     * @param options           The optional parameters
     * @return The response object.
     */
    public Response<BlockBlobItem> commitBlockListWithRestResponse(String containerName,
                                                                             String blobName,
                                                                             List<String> base64BlockIds,
                                                                             CommitBlockListOptions options) {
        BlockBlobsCommitBlockListResponse commitBlockListResponse = this.storageBlobServiceClient.commitBlockListWithRestResponse(containerName,
            blobName,
            new BlockLookupList().setCommitted(base64BlockIds),
            ClientUtil.toImplOptions(options));

        return new Response<>(null,
            commitBlockListResponse.getStatusCode(),
            commitBlockListResponse.getHeaders(),
            ClientUtil.buildBlockBlobItem(commitBlockListResponse.getDeserializedHeaders()));
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    void delete(String containerName,
                String blobName) {
        BlobDeleteResponse deleteResponse = storageBlobServiceClient.deleteWithRestResponse(containerName,
            blobName,
            new com.azure.android.storage.blob.implementation.models.BlobDeleteOptions());
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
     * @param options           The optional parameter
     * @return A response object containing the details of the delete operation.
     */
    Response<Void> deleteWithRestResponse(String containerName,
                                          String blobName,
                                          BlobDeleteOptions options) {
        BlobDeleteResponse deleteResponse = storageBlobServiceClient.deleteWithRestResponse(containerName,
            blobName,
            ClientUtil.toImplOptions(options));

        return new Response<>(null,
            deleteResponse.getStatusCode(),
            deleteResponse.getHeaders(),
            null);
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
                .addInterceptor(new AddDateInterceptor())
                .setSerializationFormat(SerializerFormat.XML);
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
