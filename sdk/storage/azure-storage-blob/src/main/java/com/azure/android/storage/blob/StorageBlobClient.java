// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.ServiceCallBack;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;

/**
 * Client for Storage Blob service.
 */
public class StorageBlobClient {
    private final ServiceClient serviceClient;
    private final StorageBlobServiceImpl storageBlobServiceClient;

    private StorageBlobClient(ServiceClient serviceClient) {
        this.serviceClient = serviceClient;
        this.storageBlobServiceClient = new StorageBlobServiceImpl(this.serviceClient);
    }

    /**
     * Creates a new {@link Builder} with initial configuration copied from this {@link StorageBlobClient}.
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
     * @param pageId Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options The page options.
     * @return A list of blobs.
     */
    public List<BlobItem> getBlobsInPage(String pageId,
                                         String containerName,
                                         ListBlobsOptions options) {
        return this.storageBlobServiceClient.getBlobsInPage(pageId, containerName, options);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options The page options.
     * @param callback Callback that receives the retrieved blob list.
     */
    public void getBlobsInPage(String pageId,
                               String containerName,
                               ListBlobsOptions options,
                               ServiceCallBack<List<BlobItem>> callback) {
        this.storageBlobServiceClient.getBlobsInPage(pageId, containerName, options, callback);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param prefix Filters the results to return only blobs whose name begins with the specified prefix.
     * @param maxResults Specifies the maximum number of blobs to return.
     * @param include Include this parameter to specify one or more datasets to include in the response.
     * @param timeout The timeout parameter is expressed in seconds. For more information, see
     * &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in
     * the analytics logs when storage analytics logging is enabled.
     * @return A response object containing a list of blobs.
     */
    public ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponse(String pageId,
                                                                                String containerName,
                                                                                String prefix,
                                                                                Integer maxResults,
                                                                                List<ListBlobsIncludeItem> include,
                                                                                Integer timeout,
                                                                                String requestId) {
        return this.storageBlobServiceClient.getBlobsInPageWithRestResponse(pageId, containerName,
                prefix,
                maxResults,
                include,
                timeout,
                requestId);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param prefix Filters the results to return only blobs whose name begins with the specified prefix.
     * @param maxResults Specifies the maximum number of blobs to return.
     * @param include Include this parameter to specify one or more datasets to include in the response.
     * @param timeout The timeout parameter is expressed in seconds. For more information, see
     * &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in
     * the analytics logs when storage analytics logging is enabled.
     * @param callback Callback that receives the response.
     */
    public void getBlobsInPageWithRestResponse(String pageId,
                                               String containerName,
                                               String prefix,
                                               Integer maxResults,
                                               List<ListBlobsIncludeItem> include,
                                               Integer timeout,
                                               String requestId,
                                               ServiceCallBack<ContainersListBlobFlatSegmentResponse> callback) {
        this.storageBlobServiceClient.getBlobsInPageWithRestResponse(pageId,
                containerName,
                prefix,
                maxResults,
                include,
                timeout,
                requestId,
                callback);
    }

    public void stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] content,
                           byte[] contentMd5) {
        this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            content,
            contentMd5);
    }

    public void stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] body,
                           byte[] contentMd5,
                           ServiceCallBack<Void> callback) {
        this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            body,
            contentMd5,
            callback);
    }

    public BlockBlobsStageBlockResponse stageBlockWithRestResponse(String containerName,
                                                                   String blobName,
                                                                   String base64BlockId,
                                                                   byte[] body,
                                                                   byte[] transactionalContentMD5,
                                                                   byte[] transactionalContentCrc64,
                                                                   Integer timeout,
                                                                   String leaseId,
                                                                   String requestId,
                                                                   CpkInfo cpkInfo) {
        return this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            body,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo);
    }

    public void stageBlockWithRestResponse(String containerName,
                                           String blobName,
                                           String base64BlockId,
                                           byte[] body,
                                           byte[] transactionalContentMD5,
                                           byte[] transactionalContentCrc64,
                                           Integer timeout,
                                           String leaseId,
                                           String requestId,
                                           CpkInfo cpkInfo,
                                           ServiceCallBack<BlockBlobsStageBlockResponse> callback) {
        this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            body,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            callback);
    }

    public BlockBlobItem commitBlockList(String containerName,
                                         String blobName,
                                         List<String> base64BlockIds,
                                         boolean overwrite) {
        return this.storageBlobServiceClient.commitBlockList(containerName,
            blobName,
            base64BlockIds,
            overwrite);
    }

    public void commitBlockList(String containerName,
                                String blobName,
                                List<String> base64BlockIds,
                                boolean overwrite,
                                ServiceCallBack<BlockBlobItem> callBack) {
        this.storageBlobServiceClient.commitBlockList(containerName,
            blobName,
            base64BlockIds,
            overwrite,
            callBack);
    }


    public BlockBlobsCommitBlockListResponse commitBlockListWithRestResponse(String containerName,
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
                                                                             AccessTier tier) {
        return this.storageBlobServiceClient.commitBlockListWithRestResponse(containerName,
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
            tier);
    }

    public void commitBlockListWithRestResponse(String containerName,
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
                                                ServiceCallBack<BlockBlobsCommitBlockListResponse> callback) {
        this.storageBlobServiceClient.commitBlockListWithRestResponse(containerName,
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
            tier, callback);
    }

    /**
     * Builder for {@link StorageBlobClient}.
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
         * @param serviceClientBuilder The {@link com.azure.android.core.http.ServiceClient.Builder}.
         */
        public Builder(ServiceClient.Builder serviceClientBuilder) {
            Objects.requireNonNull(serviceClientBuilder, "serviceClientBuilder cannot be null.");
            this.serviceClientBuilder = serviceClientBuilder;
        }

        /**
         * Sets the base URL for the {@link StorageBlobClient}.
         *
         * @param blobServiceUrl The blob service base URL.
         * @return An updated {@link Builder} with these settings applied.
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
         * @return An updated {@link Builder} with these settings applied.
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
            return new StorageBlobClient(this.serviceClientBuilder.build());
        }

        private Builder(final StorageBlobClient storageBlobClient) {
            this(storageBlobClient.serviceClient.newBuilder());
        }
    }
}
