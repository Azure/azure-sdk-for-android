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
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobGetTagsHeaders;
import com.azure.android.storage.blob.models.BlobGetTagsResponse;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersHeaders;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersResponse;
import com.azure.android.storage.blob.models.BlobSetMetadataHeaders;
import com.azure.android.storage.blob.models.BlobSetMetadataResponse;
import com.azure.android.storage.blob.models.BlobSetTagsHeaders;
import com.azure.android.storage.blob.models.BlobSetTagsResponse;
import com.azure.android.storage.blob.models.BlobSetTierHeaders;
import com.azure.android.storage.blob.models.BlobSetTierResponse;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlobTag;
import com.azure.android.storage.blob.models.BlobTags;
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
import com.azure.android.storage.blob.models.RehydratePriority;

import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final String serviceVersion;

    StorageBlobServiceImpl(ServiceClient serviceClient, String serviceVersion) {
        this.service = serviceClient.getRetrofit().create(StorageBlobService.class);
        this.serializerAdapter = SerializerAdapter.createDefault();
        this.serviceVersion = serviceVersion;
    }


    Void createContainer(String containerName) {
        return createContainerWithRestResponse(containerName,
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
            CancellationToken.NONE,
            callback);
    }

    ContainerCreateResponse createContainerWithRestResponse(String containerName,
                                                            Integer timeout,
                                                            Map<String, String> metadata,
                                                            PublicAccessType publicAccessType,
                                                            CancellationToken cancellationToken) {
        return createContainersWithRestResponseIntern(containerName,
            timeout,
            metadata,
            publicAccessType,
            cancellationToken,
            null);
    }

    void createContainer(String containerName,
                         Integer timeout,
                         Map<String, String> metadata,
                         PublicAccessType publicAccessType,
                         CancellationToken cancellationToken,
                         CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        this.createContainersWithRestResponseIntern(containerName,
            timeout,
            metadata,
            publicAccessType,
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
     * @return A response object containing the details of the delete operation.
     */
    ContainerDeleteResponse deleteContainerWithRestResponse(String containerName,
                                                            Integer timeout,
                                                            BlobRequestConditions requestConditions,
                                                            CancellationToken cancellationToken) {
        return deleteContainerWithRestResponseIntern(containerName,
            timeout,
            requestConditions,
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
     * @param callback          Callback that receives the response.
     */
    void deleteContainer(String containerName,
                         Integer timeout,
                         BlobRequestConditions requestConditions,
                         CancellationToken cancellationToken,
                         CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        deleteContainerWithRestResponseIntern(containerName,
            timeout,
            requestConditions,
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
            CancellationToken.NONE,
            callback);
    }

    /**
     * Gets the container's properties..
     *
     * @param containerName The container name.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @return A response containing the blob metadata.
     */
    ContainerGetPropertiesResponse getContainerPropertiesWithResponse(String containerName,
                                                                      Integer timeout,
                                                                      String leaseId,
                                                                      CancellationToken cancellationToken) {
        return getContainerPropertiesWithRestResponseIntern(containerName,
            timeout,
            leaseId,
            cancellationToken,
            null);
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
            CancellationToken.NONE,
            callback);
    }

    ContainersListBlobFlatSegmentResponse listBlobFlatSegmentWithRestResponse(String pageId,
                                                                              String containerName,
                                                                              String prefix,
                                                                              Integer maxResults,
                                                                              List<ListBlobsIncludeItem> include,
                                                                              Integer timeout,
                                                                              CancellationToken cancellationToken) {
        return this.listBlobFlatSegmentWithRestResponseIntern(pageId, containerName,
            prefix,
            maxResults,
            include,
            timeout,
            cancellationToken,
            null);
    }

    void listBlobFlatSegment(String pageId,
                             String containerName,
                             String prefix,
                             Integer maxResults,
                             List<ListBlobsIncludeItem> include,
                             Integer timeout,
                             CancellationToken cancellationToken,
                             CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders> callback) {
        this.listBlobFlatSegmentWithRestResponseIntern(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
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
            CancellationToken.NONE,
            callback);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param cpkInfo           Additional parameters for the operation.
     * @return A response containing the blob metadata.
     */
    BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                String blobName,
                                                                String snapshot,
                                                                Integer timeout,
                                                                BlobRequestConditions requestConditions,
                                                                CpkInfo cpkInfo,
                                                                CancellationToken cancellationToken) {
        return getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            requestConditions,
            cpkInfo,
            cancellationToken,
            null);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestConditions {@link BlobRequestConditions}
     * @param cpkInfo           Additional parameters for the operation.
     * @param callback          Callback that receives the response.
     */
    void getBlobProperties(String containerName,
                           String blobName,
                           String snapshot,
                           Integer timeout,
                           BlobRequestConditions requestConditions,
                           CpkInfo cpkInfo,
                           CancellationToken cancellationToken,
                           CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        this.getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            requestConditions,
            cpkInfo,
            cancellationToken,
            callback);
    }

    Void setBlobHttpHeaders(String containerName,
                            String blobName,
                            BlobHttpHeaders headers) {
        BlobSetHttpHeadersResponse blobSetHttpHeadersResponse = setBlobHttpHeadersWithRestResponse(containerName,
            blobName,
            null,
            null,
            headers,
            CancellationToken.NONE);

        return blobSetHttpHeadersResponse.getValue();
    }

    void setBlobHttpHeaders(String containerName,
                            String blobName,
                            BlobHttpHeaders headers,
                            CallbackWithHeader<Void, BlobSetHttpHeadersHeaders> callback) {
        setBlobHttpHeaders(containerName,
            blobName,
            null,
            null,
            headers,
            CancellationToken.NONE,
            callback);
    }

    BlobSetHttpHeadersResponse setBlobHttpHeadersWithRestResponse(String containerName,
                                                                  String blobName,
                                                                  Integer timeout,
                                                                  BlobRequestConditions requestConditions,
                                                                  BlobHttpHeaders headers,
                                                                  CancellationToken cancellationToken) {
        return setHttpHeadersWithRestResponseIntern(containerName,
            blobName,
            timeout,
            requestConditions,
            headers,
            cancellationToken,
            null);
    }

    void setBlobHttpHeaders(String containerName,
                            String blobName,
                            Integer timeout,
                            BlobRequestConditions requestConditions,
                            BlobHttpHeaders headers,
                            CancellationToken cancellationToken,
                            CallbackWithHeader<Void, BlobSetHttpHeadersHeaders> callback) {
        this.setHttpHeadersWithRestResponseIntern(containerName,
            blobName,
            timeout,
            requestConditions,
            headers,
            cancellationToken,
            callback);
    }

    Void setBlobMetadata(String containerName,
                         String blobName,
                         Map<String, String> metadata) {
        BlobSetMetadataResponse blobSetHttpHeadersResponse = setBlobMetadataWithRestResponse(containerName,
            blobName,
            null,
            null,
            metadata,
            null,
            CancellationToken.NONE);

        return blobSetHttpHeadersResponse.getValue();
    }

    void setBlobMetadata(String containerName,
                         String blobName,
                         Map<String, String> metadata,
                         CallbackWithHeader<Void, BlobSetMetadataHeaders> callback) {
        setBlobMetadata(containerName,
            blobName,
            null,
            null,
            metadata,
            null,
            CancellationToken.NONE,
            callback);
    }

    BlobSetMetadataResponse setBlobMetadataWithRestResponse(String containerName,
                                                            String blobName,
                                                            Integer timeout,
                                                            BlobRequestConditions requestConditions,
                                                            Map<String, String> metadata,
                                                            CpkInfo cpkInfo,
                                                            CancellationToken cancellationToken) {
        return setBlobMetadataWithRestResponseIntern(containerName,
            blobName,
            timeout,
            requestConditions,
            metadata,
            cpkInfo,
            cancellationToken,
            null);
    }

    void setBlobMetadata(String containerName,
                         String blobName,
                         Integer timeout,
                         BlobRequestConditions requestConditions,
                         Map<String, String> metadata,
                         CpkInfo cpkInfo,
                         CancellationToken cancellationToken,
                         CallbackWithHeader<Void, BlobSetMetadataHeaders> callback) {
        this.setBlobMetadataWithRestResponseIntern(containerName,
            blobName,
            timeout,
            requestConditions,
            metadata,
            cpkInfo,
            cancellationToken,
            callback);
    }

    /**
     * Sets the access tier of a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tier          The access tier.
     */
    Void setBlobTier(String containerName,
                     String blobName,
                     AccessTier tier) {
        BlobSetTierResponse response = setBlobTierWithRestResponse(containerName,
            blobName,
            tier,
            null,
            null,
            null,
            null,
            null,
            null,
            CancellationToken.NONE);

        return response.getValue();
    }

    /**
     * Sets the access tier of a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tier          The access tier.
     * @param callback      The callback that receives the response.
     */
    void setBlobTier(String containerName,
                     String blobName,
                     AccessTier tier,
                     CallbackWithHeader<Void, BlobSetTierHeaders> callback) {
        setBlobTierWithRestResponseIntern(containerName,
            blobName,
            tier,
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
     * The Set Blob Tier operation sets the tier on a blob. The operation is allowed on a page blob in a premium storage account and on a block blob in a blob storage account (locally redundant storage only). A premium page blob's tier determines the allowed size, IOPS, and bandwidth of the blob. A block blob's tier determines Hot/Cool/Archive storage type. This operation does not update the blob's ETag.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param tier              Indicates the tier to be set on the blob. Possible values include: 'P4', 'P6', 'P10', 'P15', 'P20', 'P30', 'P40', 'P50', 'P60', 'P70', 'P80', 'Hot', 'Cool', 'Archive'.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param versionId         The version id parameter is an opaque DateTime value that, when present, specifies the version of the blob to operate on. It's for service version 2019-10-10 and newer.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param rehydratePriority Optional: Indicates the priority with which to rehydrate an archived blob. Possible values include: 'High', 'Standard'.
     * @param leaseId           If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param ifTags            Specify a SQL where clause on blob tags to operate only on blobs with a matching value.
     * @param cancellationToken The token to request cancellation.
     * @return The response information returned from the server when setting tier on a blob.
     */
    BlobSetTierResponse setBlobTierWithRestResponse(String containerName,
                                                    String blobName,
                                                    AccessTier tier,
                                                    String snapshot,
                                                    String versionId,
                                                    Integer timeout,
                                                    RehydratePriority rehydratePriority,
                                                    String leaseId,
                                                    String ifTags,
                                                    CancellationToken cancellationToken) {
        return setBlobTierWithRestResponseIntern(containerName,
            blobName,
            tier,
            snapshot,
            versionId,
            timeout,
            rehydratePriority,
            leaseId,
            ifTags,
            cancellationToken,
            null);
    }

    /**
     * The Set Blob Tier operation sets the tier on a blob. The operation is allowed on a page blob in a premium storage account and on a block blob in a blob storage account (locally redundant storage only). A premium page blob's tier determines the allowed size, IOPS, and bandwidth of the blob. A block blob's tier determines Hot/Cool/Archive storage type. This operation does not update the blob's ETag.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param tier              Indicates the tier to be set on the blob. Possible values include: 'P4', 'P6', 'P10', 'P15', 'P20', 'P30', 'P40', 'P50', 'P60', 'P70', 'P80', 'Hot', 'Cool', 'Archive'.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param versionId         The version id parameter is an opaque DateTime value that, when present, specifies the version of the blob to operate on. It's for service version 2019-10-10 and newer.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param rehydratePriority Optional: Indicates the priority with which to rehydrate an archived blob. Possible values include: 'High', 'Standard'.
     * @param leaseId           If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param ifTags            Specify a SQL where clause on blob tags to operate only on blobs with a matching value.
     * @param cancellationToken The token to request cancellation.
     * @param callback          Callback that receives the response.
     */
    void setBlobTier(String containerName,
                     String blobName,
                     AccessTier tier,
                     String snapshot,
                     String versionId,
                     Integer timeout,
                     RehydratePriority rehydratePriority,
                     String leaseId,
                     String ifTags,
                     CancellationToken cancellationToken,
                     CallbackWithHeader<Void, BlobSetTierHeaders> callback) {
        this.setBlobTierWithRestResponseIntern(containerName,
            blobName,
            tier,
            snapshot,
            versionId,
            timeout,
            rehydratePriority,
            leaseId,
            ifTags,
            cancellationToken,
            callback);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param callback      Callback that receives the response.
     */
    void getContainerProperties(String containerName,
                                Integer timeout,
                                String leaseId,
                                CancellationToken cancellationToken,
                                CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        this.getContainerPropertiesWithRestResponseIntern(containerName,
            timeout,
            leaseId,
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
                                                            Boolean computeMd5,
                                                            Integer timeout,
                                                            String leaseId,
                                                            CpkInfo cpkInfo,
                                                            CancellationToken cancellationToken) {
        return this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            computeMd5,
            timeout,
            leaseId,
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
                    Boolean computeMd5,
                    Integer timeout,
                    String leaseId,
                    CpkInfo cpkInfo,
                    CancellationToken cancellationToken,
                    CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            computeMd5,
            timeout,
            leaseId,
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
     * @param deleteSnapshots   Required if the blob has associated snapshots. Specify one of the following two
     *                          options: include: Delete the base blob and all of its snapshots. only: Delete only the blob's snapshots and not the blob itself. Possible values include: 'include', 'only'.
     * @param requestConditions {@link BlobRequestConditions}
     * @return A response object containing the details of the delete operation.
     */
    BlobDeleteResponse deleteBlobWithRestResponse(String containerName,
                                                  String blobName,
                                                  String snapshot,
                                                  Integer timeout,
                                                  DeleteSnapshotsOptionType deleteSnapshots,
                                                  BlobRequestConditions requestConditions,
                                                  CancellationToken cancellationToken) {
        return deleteBlobWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            deleteSnapshots,
            requestConditions,
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
     * @param deleteSnapshots   Required if the blob has associated snapshots. Specify one of the following two
     *                          options: include: Delete the base blob and all of its snapshots. only: Delete only the blob's snapshots and not the blob itself. Possible values include: 'include', 'only'.
     * @param requestConditions {@link BlobRequestConditions}
     * @param callback          Callback that receives the response.
     * @return A handle to the service call.
     */
    void deleteBlob(String containerName,
                    String blobName,
                    String snapshot,
                    Integer timeout,
                    DeleteSnapshotsOptionType deleteSnapshots,
                    BlobRequestConditions requestConditions,
                    CancellationToken cancellationToken,
                    CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        deleteBlobWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            timeout,
            deleteSnapshots,
            requestConditions,
            cancellationToken,
            callback);
    }

    /**
     * Gets tags associated with a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return A response containing the blob tags information.
     */
    BlobTags getTags(String containerName,
                     String blobName) {
        return getTagsWithRestResponse(containerName,
            blobName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE).getValue();
    }

    /**
     * Gets tags associated with a blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    void getTags(String containerName,
                 String blobName,
                 CallbackWithHeader<BlobTags, BlobGetTagsHeaders> callback) {
        getTags(containerName,
            blobName,
            null,
            null,
            null,
            null,
            CancellationToken.NONE,
            callback);
    }

    /**
     * Gets tags associated with a blob.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the
     *                          blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param versionId         The version id parameter is an opaque DateTime value that, when present, specifies the version of the blob to operate on. It's for service version 2019-10-10 and newer.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param ifTags            Specify a SQL where clause on blob tags to operate only on blobs with a matching value.
     * @param cancellationToken The token to request cancellation.
     * @return The response information returned from the server when getting tags on a blob.
     */
    BlobGetTagsResponse getTagsWithRestResponse(String containerName,
                                                String blobName,
                                                String snapshot,
                                                String versionId,
                                                Integer timeout,
                                                String ifTags,
                                                CancellationToken cancellationToken) {
        return getTagsWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            versionId,
            timeout,
            ifTags,
            cancellationToken,
            null);
    }


    /**
     * Gets tags associated with a blob.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param snapshot          The snapshot parameter is an opaque DateTime value that, when present, specifies the
     *                          blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param versionId         The version id parameter is an opaque DateTime value that, when present, specifies the version of the blob to operate on. It's for service version 2019-10-10 and newer.
     * @param timeout           The timeout parameter is expressed in seconds. For more information, see
     *                          &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param ifTags            Specify a SQL where clause on blob tags to operate only on blobs with a matching value.
     * @param cancellationToken The token to request cancellation.
     * @param callback          Callback that receives the response.
     */
    void getTags(String containerName,
                 String blobName,
                 String snapshot,
                 String versionId,
                 Integer timeout,
                 String ifTags,
                 CancellationToken cancellationToken,
                 CallbackWithHeader<BlobTags, BlobGetTagsHeaders> callback) {
        getTagsWithRestResponseIntern(containerName,
            blobName,
            snapshot,
            versionId,
            timeout,
            ifTags,
            cancellationToken,
            callback);
    }

    Void setBlobTags(String containerName,
                     String blobName,
                     Map<String, String> tags) {
        BlobSetTagsResponse blobSetHttpHeadersResponse = setBlobTagsWithRestResponse(containerName,
            blobName,
            null,
            null,
            null,
            tags,
            CancellationToken.NONE);

        return blobSetHttpHeadersResponse.getValue();
    }

    void setBlobTags(String containerName,
                     String blobName,
                     Map<String, String> tags,
                     CallbackWithHeader<Void, BlobSetTagsHeaders> callback) {
        setBlobTags(containerName,
            blobName,
            null,
            null,
            null,
            tags,
            CancellationToken.NONE,
            callback);
    }

    BlobSetTagsResponse setBlobTagsWithRestResponse(String containerName,
                                                    String blobName,
                                                    Integer timeout,
                                                    String versionId,
                                                    String ifTags,
                                                    Map<String, String> tags,
                                                    CancellationToken cancellationToken) {
        return setBlobTagsWithRestResponseIntern(containerName,
            blobName,
            timeout,
            versionId,
            ifTags,
            tags,
            cancellationToken,
            null);
    }

    void setBlobTags(String containerName,
                     String blobName,
                     Integer timeout,
                     String versionId,
                     String ifTags,
                     Map<String, String> tags,
                     CancellationToken cancellationToken,
                     CallbackWithHeader<Void, BlobSetTagsHeaders> callback) {
        this.setBlobTagsWithRestResponseIntern(containerName,
            blobName,
            timeout,
            versionId,
            ifTags,
            tags,
            cancellationToken,
            callback);
    }

    private ContainerCreateResponse createContainersWithRestResponseIntern(String containerName,
                                                                           Integer timeout,
                                                                           Map<String, String> metadata,
                                                                           PublicAccessType publicAccessType,
                                                                           CancellationToken cancellationToken,
                                                                           CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        Call<ResponseBody> call = service.createContainer(containerName,
            timeout,
            metadata == null ? null : new MetadataInterceptor.StorageMultiHeaders(metadata),
            publicAccessType,
            serviceVersion,
            null,
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

    private ContainerDeleteResponse deleteContainerWithRestResponseIntern(String containerName,
                                                                          Integer timeout,
                                                                          BlobRequestConditions requestConditions,
                                                                          CancellationToken cancellationToken,
                                                                          CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String restype = "container";
        requestConditions = requestConditions == null ? new BlobRequestConditions() : requestConditions;
        if (!validateNoETag(requestConditions)) {
            // Throwing is preferred to Single.error because this will error out immediately instead of waiting until
            // subscription.
            throw new UnsupportedOperationException("ETag access conditions are not supported for this API.");
        }

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
            serviceVersion,
            null);

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

    private ContainerGetPropertiesResponse getContainerPropertiesWithRestResponseIntern(String containerName,
                                                                                        Integer timeout,
                                                                                        String leaseId,
                                                                                        CancellationToken cancellationToken,
                                                                                        CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String restype = "container";

        Call<Void> call = service.getContainerProperties(containerName,
            timeout,
            serviceVersion,
            leaseId,
            null,
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

    private ContainersListBlobFlatSegmentResponse listBlobFlatSegmentWithRestResponseIntern(String pageId,
                                                                                            String containerName,
                                                                                            String prefix,
                                                                                            Integer maxResults,
                                                                                            List<ListBlobsIncludeItem> include,
                                                                                            Integer timeout,
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
            serviceVersion,
            null,
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
                                                                              BlobRequestConditions requestConditions,
                                                                              CpkInfo cpkInfo,
                                                                              CancellationToken cancellationToken,
                                                                              CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
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
            serviceVersion,
            leaseId,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            null,
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

    private BlobSetHttpHeadersResponse setHttpHeadersWithRestResponseIntern(String containerName,
                                                                            String blobName,
                                                                            Integer timeout,
                                                                            BlobRequestConditions requestConditions,
                                                                            BlobHttpHeaders headers,
                                                                            CancellationToken cancellationToken,
                                                                            CallbackWithHeader<Void, BlobSetHttpHeadersHeaders> callback) {

        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        final String comp = "properties";

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

        headers = headers == null ? new BlobHttpHeaders() : headers;

        Call<ResponseBody> call = service.setBlobHttpHeaders(containerName,
            blobName,
            timeout,
            leaseId,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            null, // TODO: Add tags when later service version supported.
            serviceVersion,
            null,
            comp,
            headers.getCacheControl(),
            headers.getContentType(),
            headers.getContentMd5() == null ? null : Base64Util.encodeToString(headers.getContentMd5()),
            headers.getContentEncoding(),
            headers.getContentLanguage(),
            headers.getContentDisposition());

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            BlobSetHttpHeadersHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobSetHttpHeadersHeaders.class);

                            callback.onSuccess(null,
                                typedHeaders,
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
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    BlobSetHttpHeadersHeaders deserializedHeaders = deserializeHeaders(response.headers(),
                        BlobSetHttpHeadersHeaders.class);

                    BlobSetHttpHeadersResponse result = new BlobSetHttpHeadersResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializedHeaders);

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

    private BlobSetMetadataResponse setBlobMetadataWithRestResponseIntern(String containerName,
                                                                          String blobName,
                                                                          Integer timeout,
                                                                          BlobRequestConditions requestConditions,
                                                                          Map<String, String> metadata,
                                                                          CpkInfo cpkInfo,
                                                                          CancellationToken cancellationToken,
                                                                          CallbackWithHeader<Void, BlobSetMetadataHeaders> callback) {

        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        String encryptionKey = null;
        String encryptionKeySha256 = null;
        EncryptionAlgorithmType encryptionAlgorithm = null;
        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }

        final String comp = "metadata";

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

        Call<ResponseBody> call = service.setBlobMetadata(containerName,
            blobName,
            timeout,
            metadata == null ? null : new MetadataInterceptor.StorageMultiHeaders(metadata),
            leaseId,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            null, // TODO: Add tags when later service version supported.
            serviceVersion,
            null,
            comp,
            encryptionKey,
            encryptionKeySha256,
            encryptionAlgorithm,
            null // Todo: Add encryption scope with later service version
        );

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            BlobSetMetadataHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobSetMetadataHeaders.class);

                            callback.onSuccess(null,
                                typedHeaders,
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
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    BlobSetMetadataHeaders deserializedHeaders = deserializeHeaders(response.headers(),
                        BlobSetMetadataHeaders.class);

                    BlobSetMetadataResponse result = new BlobSetMetadataResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializedHeaders);

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

    private BlobSetTierResponse setBlobTierWithRestResponseIntern(String containerName,
                                                                  String blobName,
                                                                  AccessTier tier,
                                                                  String snapshot,
                                                                  String versionId,
                                                                  Integer timeout,
                                                                  RehydratePriority rehydratePriority,
                                                                  String leaseId,
                                                                  String ifTags,
                                                                  CancellationToken cancellationToken,
                                                                  CallbackWithHeader<Void, BlobSetTierHeaders> callback) {
        Objects.requireNonNull(tier);

        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        final String comp = "tier";

        Call<Void> call = service.setBlobTier(containerName,
            blobName,
            snapshot,
            versionId,
            timeout,
            serviceVersion,
            tier,
            rehydratePriority,
            null,
            leaseId,
            ifTags,
            comp
        );

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200 || response.code() == 202) {
                            BlobSetTierHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobSetTierHeaders.class);

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
                if (response.code() == 200 || response.code() == 202) {
                    BlobSetTierHeaders headers = deserializeHeaders(response.headers(),
                        BlobSetTierHeaders.class);

                    BlobSetTierResponse result = new BlobSetTierResponse(response.raw().request(),
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
            serviceVersion,
            null,
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
                                                                          Boolean computeMd5,
                                                                          Integer timeout,
                                                                          String leaseId,
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

        if (computeMd5 != null && computeMd5) {
            if (transactionalContentMD5 != null) {
                throw new IllegalArgumentException("'transactionalContentMD5' can not be set when 'computeMd5' is true.");
            }
            try {
                transactionalContentMD5 = MessageDigest.getInstance("MD5").digest(blockContent);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

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
            serviceVersion,
            null,
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
            metadata == null ? null : new MetadataInterceptor.StorageMultiHeaders(metadata),
            leaseId,
            tier,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            blocks,
            serviceVersion,
            null,
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
                                                                DeleteSnapshotsOptionType deleteSnapshots,
                                                                BlobRequestConditions requestConditions,
                                                                CancellationToken cancellationToken,
                                                                CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
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

        Call<ResponseBody> call = service.deleteBlob(containerName,
            blobName,
            snapshot,
            timeout,
            leaseId,
            deleteSnapshots,
            ifModifiedSince,
            ifUnmodifiedSince,
            ifMatch,
            ifNoneMatch,
            serviceVersion,
            null);

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

    private BlobGetTagsResponse getTagsWithRestResponseIntern(String containerName,
                                                              String blobName,
                                                              String snapshot,
                                                              String versionId,
                                                              Integer timeout,
                                                              String ifTags,
                                                              CancellationToken cancellationToken,
                                                              CallbackWithHeader<BlobTags, BlobGetTagsHeaders> callback) {
        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;
        final String comp = "tags";

        Call<ResponseBody> call = service.getTags(containerName,
            blobName,
            snapshot,
            versionId,
            timeout,
            comp,
            serviceVersion,
            null,
            ifTags);

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            BlobTags typedContent = deserializeContent(response.body(),
                                BlobTags.class);

                            BlobGetTagsHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobGetTagsHeaders.class);

                            callback.onSuccess(typedContent, typedHeaders, response.raw());
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
                if (response.code() == 200) {
                    BlobTags typedContent = deserializeContent(response.body(),
                        BlobTags.class);

                    BlobGetTagsHeaders headers = deserializeHeaders(response.headers(),
                        BlobGetTagsHeaders.class);

                    BlobGetTagsResponse result = new BlobGetTagsResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        typedContent,
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

    private BlobSetTagsResponse setBlobTagsWithRestResponseIntern(String containerName,
                                                                  String blobName,
                                                                  Integer timeout,
                                                                  String versionId,
                                                                  String iftags,
                                                                  Map<String, String> tags,
                                                                  CancellationToken cancellationToken,
                                                                  CallbackWithHeader<Void, BlobSetTagsHeaders> callback) {

        cancellationToken = cancellationToken == null ? CancellationToken.NONE : cancellationToken;

        final String comp = "tags";

        List<BlobTag> blobTagSet = null;
        if (tags != null) {
            blobTagSet = new ArrayList<>(tags.size());
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                blobTagSet.add(new BlobTag().setKey(entry.getKey()).setValue(entry.getValue()));
            }
        }
        BlobTags blobTags = new BlobTags();
        blobTags.setBlobTagSet(blobTagSet);

        RequestBody tagsBody;
        try {
            tagsBody = RequestBody.create(MediaType.get("application/xml; charset=utf-8"),
                serializerAdapter.serialize(blobTags, SerializerFormat.XML));
        } catch (IOException ioe) {
            if (callback != null) {
                callback.onFailure(ioe, null);

                return null;
            } else {
                throw new RuntimeException(ioe);
            }
        }

        Call<ResponseBody> call = service.setBlobTags(containerName,
            blobName,
            timeout,
            versionId,
            null,
            null,
            iftags,
            serviceVersion,
            null,
            tagsBody,
            comp
        );

        ((CancellationTokenImpl) cancellationToken).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 204) {
                            BlobSetTagsHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobSetTagsHeaders.class);

                            callback.onSuccess(null,
                                typedHeaders,
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
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t, null);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 204) {
                    BlobSetTagsHeaders deserializedHeaders = deserializeHeaders(response.headers(),
                        BlobSetTagsHeaders.class);

                    BlobSetTagsResponse result = new BlobSetTagsResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializedHeaders);

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

        @DELETE("{containerName}")
        Call<ResponseBody> deleteContainer(@Path("containerName") String containerName,
                                           @Query("restype") String restype,
                                           @Query("timeout") Integer timeout,
                                           @Header("x-ms-lease-id") String leaseId,
                                           @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                           @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                           @Header("x-ms-version") String version,
                                           @Header("x-ms-client-request-id") String requestId);

        @GET("{containerName}")
        Call<Void> getContainerProperties(@Path("containerName") String containerName,
                                          @Query("timeout") Integer timeout,
                                          @Header("x-ms-version") String version,
                                          @Header("x-ms-lease-id") String leaseId,
                                          @Header("x-ms-client-request-id") String requestId,
                                          @Query("restype") String resType);

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
                                     @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                     @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                     @Header("If-Match") String ifMatch,
                                     @Header("If-None-Match") String ifNoneMatch,
                                     @Header("x-ms-client-request-id") String requestId,
                                     @Header("x-ms-encryption-key") String encryptionKey,
                                     @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                     @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);

        @PUT("{containerName}/{blob}")
        Call<ResponseBody> setBlobHttpHeaders(@Path("containerName") String containerName,
                                              @Path("blob") String blob,
                                              @Query("timeout") Integer timeout,
                                              @Header("x-ms-lease-id") String leaseId,
                                              @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                              @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                              @Header("If-Match") String ifMatch,
                                              @Header("If-None-Match") String ifNoneMatch,
                                              @Header("x-ms-if-tags") String ifTags,
                                              @Header("x-ms-version") String version,
                                              @Header("x-ms-client-request-id") String requestId,
                                              @Query("comp") String comp,
                                              @Header("x-ms-blob-cache-control") String cacheControl,
                                              @Header("x-ms-blob-content-type") String contentType,
                                              @Header("x-ms-blob-content-md5") String contentMd5,
                                              @Header("x-ms-blob-content-encoding") String contentEncoding,
                                              @Header("x-ms-blob-content-language") String contentLanguage,
                                              @Header("x-ms-blob-content-disposition") String contentDisposition);

        @PUT("{containerName}/{blob}")
        Call<ResponseBody> setBlobMetadata(@Path("containerName") String containerName,
                                           @Path("blob") String blob,
                                           @Query("timeout") Integer timeout,
                                           @Tag MetadataInterceptor.StorageMultiHeaders metadata,
                                           @Header("x-ms-lease-id") String leaseId,
                                           @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince,
                                           @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince,
                                           @Header("If-Match") String ifMatch,
                                           @Header("If-None-Match") String ifNoneMatch,
                                           @Header("x-ms-if-tags") String ifTags,
                                           @Header("x-ms-version") String version,
                                           @Header("x-ms-client-request-id") String requestId,
                                           @Query("comp") String comp,
                                           @Header("x-ms-encryption-key") String encryptionKey,
                                           @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                           @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm,
                                           @Header("x-ms-encryption-scope") String encryptionScope);

        @PUT("{containerName}/{blob}")
        Call<Void> setBlobTier(@Path("containerName") String containerName,
                               @Path("blob") String blob,
                               @Query("snapshot") String snapshot,
                               @Query("versionid") String versionId,
                               @Query("timeout") Integer timeout,
                               @Header("x-ms-version") String version,
                               @Header("x-ms-access-tier") AccessTier tier,
                               @Header("x-ms-rehydrate-priority") RehydratePriority rehydratePriority,
                               @Header("x-ms-client-request-id") String requestId,
                               @Header("x-ms-lease-id") String leaseId,
                               @Header("x-ms-if-tags") String ifTags,
                               @Query("comp") String comp);


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
                                           @Tag MetadataInterceptor.StorageMultiHeaders metadata,
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

        @GET("{containerName}/{blob}")
        Call<ResponseBody> getTags(@Path("containerName") String containerName,
                                   @Path("blob") String blobName,
                                   @Query("snapshot") String snapshot,
                                   @Query("versionid") String versionId,
                                   @Query("timeout") Integer timeout,
                                   @Query("comp") String comp,
                                   @Header("x-ms-version") String version,
                                   @Header("x-ms-client-request-id") String requestId,
                                   @Header("x-ms-if-tags") String ifTags);

        @PUT("{containerName}/{blob}")
        Call<ResponseBody> setBlobTags(@Path("containerName") String containerName,
                                       @Path("blob") String blob,
                                       @Query("timeout") Integer timeout,
                                       @Query("version") String versionId,
                                       @Header("Content-MD5") String transactionalContentMd5,
                                       @Header("x-ms-content-crc64") String transactionalContentCrc64,
                                       @Header("x-ms-if-tags") String ifTags,
                                       @Header("x-ms-version") String version,
                                       @Header("x-ms-client-request-id") String requestId,
                                       @Body RequestBody tags,
                                       @Query("comp") String comp);
    }

    private boolean validateNoETag(BlobRequestConditions modifiedRequestConditions) {
        if (modifiedRequestConditions == null) {
            return true;
        }
        return modifiedRequestConditions.getIfMatch() == null && modifiedRequestConditions.getIfNoneMatch() == null;
    }
}
