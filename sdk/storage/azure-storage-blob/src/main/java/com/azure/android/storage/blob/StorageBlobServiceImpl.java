// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.CancellationTokenImpl;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.Base64Util;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.storage.blob.implementation.models.BlobDeleteOptions;
import com.azure.android.storage.blob.implementation.models.CommitBlockListOptions;
import com.azure.android.storage.blob.implementation.models.StageBlockOptions;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.BlockLookupList;
import com.azure.android.storage.blob.models.ContainerListBlobFlatSegmentHeaders;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.android.storage.blob.models.EncryptionAlgorithmType;
import com.azure.android.storage.blob.implementation.models.GetBlobPropertiesOptions;
import com.azure.android.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

/**
 * PACKAGE PRIVATE CLASS AND METHODS
 */
final class StorageBlobServiceImpl {
    private final StorageBlobService service;
    private final SerializerAdapter serializerAdapter;
    private static String XMS_VERSION = "2019-02-02";

    StorageBlobServiceImpl(ServiceClient serviceClient) {
        this.service = serviceClient.getRetrofit().create(StorageBlobService.class);
        this.serializerAdapter = serviceClient.getSerializerAdapter();
    }

    String getVersion() {
        return XMS_VERSION;
    }

    List<BlobItem> getBlobsInPage(String pageId,
                                  String containerName,
                                  ListBlobsOptions options) {
        options = options == null ? new ListBlobsOptions() : options;

        ContainersListBlobFlatSegmentResponse response = this.getBlobsInPageWithRestResponse(pageId,
            containerName,
            options.getPrefix(),
            options.getMaxResultsPerPage(),
            options.getDetails().toList(),
            null,
            null,
            CancellationToken.NONE);

        return response.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : response.getValue().getSegment().getBlobItems();
    }

    void getBlobsInPage(String pageId,
                        String containerName,
                        ListBlobsOptions options,
                        Callback<List<BlobItem>> callback) {
        options = options == null ? new ListBlobsOptions() : options;

        this.getBlobsInPageWithRestResponse(pageId,
            containerName,
            options.getPrefix(),
            options.getMaxResultsPerPage(),
            options.getDetails().toList(),
            null,
            null,
            CancellationToken.NONE,
            new Callback<ContainersListBlobFlatSegmentResponse>() {
                @Override
                public void onResponse(ContainersListBlobFlatSegmentResponse response) {
                    List<BlobItem> value = response.getValue().getSegment() == null
                        ? new ArrayList<>(0)
                        : response.getValue().getSegment().getBlobItems();

                    callback.onResponse(value);
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
    }

    ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponse(String pageId,
                                                                         String containerName,
                                                                         String prefix,
                                                                         Integer maxResults,
                                                                         List<ListBlobsIncludeItem> include,
                                                                         Integer timeout,
                                                                         String requestId,
                                                                         CancellationToken cancellationToken) {
        return this.getBlobsInPageWithRestResponseIntern(pageId, containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            cancellationToken,
            null);
    }

    void getBlobsInPageWithRestResponse(String pageId,
                                        String containerName,
                                        String prefix,
                                        Integer maxResults,
                                        List<ListBlobsIncludeItem> include,
                                        Integer timeout,
                                        String requestId,
                                        CancellationToken cancellationToken,
                                        Callback<ContainersListBlobFlatSegmentResponse> callback) {
        this.getBlobsInPageWithRestResponseIntern(pageId,
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
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param options       The optional parameters.
     * @return A response containing the blob metadata.
     */
    // Response<T>
    BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                String blobName,
                                                                GetBlobPropertiesOptions options) {
        return getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            options,
            null);
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
        this.getBlobProperties(containerName,
            blobName,
            new GetBlobPropertiesOptions(),
            callback);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param options       The optional parameters.
     * @param callback      Callback that receives the response.
     */
    void getBlobProperties(String containerName,
                           String blobName,
                           GetBlobPropertiesOptions options,
                           CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        this.getBlobPropertiesWithRestResponseIntern(containerName,
            blobName,
            options,
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
                  Callback<ResponseBody> callback) {
        downloadWithRestResponse(containerName,
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
            new Callback<BlobDownloadResponse>() {
                @Override
                public void onResponse(BlobDownloadResponse response) {
                    callback.onResponse(response.getValue());
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
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
    void downloadWithRestResponse(String containerName,
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
                                  Callback<BlobDownloadResponse> callback) {
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

    BlockBlobsStageBlockResponse stageBlockWithRestResponse(String containerName,
                                                            String blobName,
                                                            String base64BlockId,
                                                            int contentLength,
                                                            byte[] blockContent,
                                                            StageBlockOptions options) {
        return this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            contentLength,
            blockContent,
            options,
            null);
    }

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    int contentLength,
                    byte[] blockContent,
                    CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlock(containerName,
            blobName,
            base64BlockId,
            contentLength,
            blockContent,
            new StageBlockOptions().setCancellationToken(CancellationToken.NONE),
            callback);
    }

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    int contentLength,
                    byte[] blockContent,
                    StageBlockOptions options,
                    CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            contentLength,
            blockContent,
            options,
            callback);
    }

    BlockBlobsCommitBlockListResponse commitBlockListWithRestResponse(String containerName,
                                                                      String blobName,
                                                                      BlockLookupList blockLookupList,
                                                                      CommitBlockListOptions options) {
        return this.commitBlockListWithRestResponseIntern2(containerName,
            blobName,
            blockLookupList,
            options,
            null);
    }

    void commitBlockList(String containerName,
                         String blobName,
                         BlockLookupList blockLookupList,
                         CallbackWithHeader<Void, BlockBlobCommitBlockListHeaders> callback) {
        this.commitBlockListWithRestResponseIntern2(containerName,
            blobName,
            blockLookupList,
            new CommitBlockListOptions().setCancellationToken(CancellationToken.NONE),
            callback);
    }

    void commitBlockList(String containerName,
                         String blobName,
                         BlockLookupList blockLookupList,
                         CommitBlockListOptions options,
                         CallbackWithHeader<Void, BlockBlobCommitBlockListHeaders> callback) {
        this.commitBlockListWithRestResponseIntern2(containerName,
            blobName,
            blockLookupList,
            options,
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
     * @param options           The optional parameter.
     * @return A response object containing the details of the delete operation.
     */
    BlobDeleteResponse deleteWithRestResponse(String containerName,
                                              String blobName,
                                              BlobDeleteOptions options) {
        return deleteWithRestResponseIntern(containerName,
            blobName,
            options,
            null);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     * @return A handle to the service call.
     */
    void delete(String containerName,
                String blobName,
                CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        delete(containerName,
            blobName,
            new BlobDeleteOptions().setCancellationToken(CancellationToken.NONE),
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
     * @param options           The optional parameter.
     * @param callback          Callback that receives the response.
     * @return A handle to the service call.
     */
    void delete(String containerName,
                String blobName,
                BlobDeleteOptions options,
                CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        deleteWithRestResponseIntern(containerName,
            blobName,
            options,
            callback);
    }

    private ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponseIntern(String pageId,
                                                                                       String containerName,
                                                                                       String prefix,
                                                                                       Integer maxResults,
                                                                                       List<ListBlobsIncludeItem> include,
                                                                                       Integer timeout,
                                                                                       String requestId,
                                                                                       CancellationToken cancellationToken,
                                                                                       Callback<ContainersListBlobFlatSegmentResponse> callback) {
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
                            ContainerListBlobFlatSegmentHeaders typedHeader = deserializeHeaders(response.headers(),
                                ContainerListBlobFlatSegmentHeaders.class);
                            callback.onResponse(new ContainersListBlobFlatSegmentResponse(response.raw().request(),
                                response.code(),
                                response.headers(),
                                typedContent,
                                typedHeader));
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()));
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure(t);
                }
            });

            return null;
        } else {
            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    ListBlobsFlatSegmentResponse typedContent = deserializeContent(response.body(),
                        ListBlobsFlatSegmentResponse.class);
                    ContainerListBlobFlatSegmentHeaders typedHeader = deserializeHeaders(response.headers(),
                        ContainerListBlobFlatSegmentHeaders.class);

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
                                                                              GetBlobPropertiesOptions options,
                                                                              CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        Call<Void> call = service.getBlobProperties(containerName,
            blobName,
            options.getSnapshot(),
            options.getTimeout(),
            this.getVersion(),
            options.getLeaseId(),
            options.getIfModifiedSince() != null ? new DateTimeRfc1123(options.getIfModifiedSince()).toString() : null,
            options.getIfUnmodifiedSince() != null ? new DateTimeRfc1123(options.getIfUnmodifiedSince()).toString() : null,
            options.getIfMatch(),
            options.getIfNoneMatch(),
            options.getRequestId(),
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionKey() : null,
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionKeySha256() : null,
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionAlgorithm() : null);

        ((CancellationTokenImpl) options.getCancellationToken()).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {
                            callback.onSuccess(null, // result is void
                                deserializeHeaders(response.headers(), BlobGetPropertiesHeaders.class),
                                response.raw());
                        } else {
                            callback.onFailure(new BlobStorageException(null, response.raw()),
                                response.raw());
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()),
                            response.raw());
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
                    BlobGetPropertiesResponse result = new BlobGetPropertiesResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializeHeaders(response.headers(), BlobGetPropertiesHeaders.class));

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
                                                                Callback<BlobDownloadResponse> callback) {
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

                            callback.onResponse(new BlobDownloadResponse(response.raw().request(),
                                response.code(),
                                response.headers(),
                                response.body(),
                                typedHeaders));
                        } else {
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()));
                        }
                    } else {
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t);
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
                                                                          int contentLength,
                                                                          byte[] blockContent,
                                                                          StageBlockOptions options,
                                                                          CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        final String comp = "block";
        //
        // int contentLength = blockContent.length;
        RequestBody body = RequestBody.create(MediaType.get("application/octet-stream"), blockContent);

        Call<ResponseBody> call = service.stageBlock(containerName,
            blobName,
            base64BlockId,
            contentLength,
            body,
            Base64Util.encodeToString(options.getTransactionalContentMD5()),
            Base64Util.encodeToString(options.getTransactionalContentCrc64()),
            options.getTimeout(),
            options.getLeaseId(),
            this.getVersion(),
            options.getRequestId(),
            comp,
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionKey() : null,
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionKeySha256() : null,
            options.getCpkInfo() != null ? options.getCpkInfo().getEncryptionAlgorithm() : null);

        ((CancellationTokenImpl) options.getCancellationToken()).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            callback.onSuccess(null, deserializeHeaders(response.headers(),
                                BlockBlobStageBlockHeaders.class), response.raw());
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
                    return new BlockBlobsStageBlockResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializeHeaders(response.headers(), BlockBlobStageBlockHeaders.class));
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

    private BlockBlobsCommitBlockListResponse commitBlockListWithRestResponseIntern2(String containerName,
                                                                                     String blobName,
                                                                                     BlockLookupList blockLookupList,
                                                                                     CommitBlockListOptions options,
                                                                                     CallbackWithHeader<Void, BlockBlobCommitBlockListHeaders> callback) {
        final String comp = "blocklist";

        String cacheControl = null;
        if (options.getBlobHttpHeaders() != null) {
            cacheControl = options.getBlobHttpHeaders().getCacheControl();
        }

        String contentType = null;
        if (options.getBlobHttpHeaders() != null) {
            contentType = options.getBlobHttpHeaders().getContentType();
        }

        String contentEncoding = null;
        if (options.getBlobHttpHeaders() != null) {
            contentEncoding = options.getBlobHttpHeaders().getContentEncoding();
        }

        String contentLanguage = null;
        if (options.getBlobHttpHeaders() != null) {
            contentLanguage = options.getBlobHttpHeaders().getContentLanguage();
        }

        byte[] contentMd5 = null;
        if (options.getBlobHttpHeaders() != null) {
            contentMd5 = options.getBlobHttpHeaders().getContentMd5();
        }

        String contentDisposition = null;
        if (options.getBlobHttpHeaders() != null) {
            contentDisposition = options.getBlobHttpHeaders().getContentDisposition();
        }

        String encryptionKey = null;
        if (options.getCpkInfo() != null) {
            encryptionKey = options.getCpkInfo().getEncryptionKey();
        }

        String encryptionKeySha256 = null;
        if (options.getCpkInfo() != null) {
            encryptionKeySha256 = options.getCpkInfo().getEncryptionKeySha256();
        }

        EncryptionAlgorithmType encryptionAlgorithm = null;
        if (options.getCpkInfo() != null) {
            encryptionAlgorithm = options.getCpkInfo().getEncryptionAlgorithm();
        }

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

        String transactionalContentMD5Converted = Base64Util.encodeToString(options.getTransactionalContentMD5());
        String transactionalContentCrc64Converted = Base64Util.encodeToString(options.getTransactionalContentCrc64());
        DateTimeRfc1123 ifModifiedSinceConverted = options.getIfModifiedSince() == null ? null : new DateTimeRfc1123(options.getIfModifiedSince());
        DateTimeRfc1123 ifUnmodifiedSinceConverted = options.getIfUnmodifiedSince() == null ? null : new DateTimeRfc1123(options.getIfUnmodifiedSince());
        String contentMd5Converted = Base64Util.encodeToString(contentMd5);

        Call<ResponseBody> call = service.commitBlockList(containerName,
            blobName,
            options.getTimeout(),
            transactionalContentMD5Converted,
            transactionalContentCrc64Converted,
            options.getMetadata(),
            options.getLeaseId(),
            options.getTier(),
            ifModifiedSinceConverted,
            ifUnmodifiedSinceConverted,
            options.getIfMatch(),
            options.getIfNoneMatch(),
            blocks,
            this.getVersion(),
            options.getRequestId(),
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


        ((CancellationTokenImpl) options.getCancellationToken()).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            callback.onSuccess(null, deserializeHeaders(response.headers(), BlockBlobCommitBlockListHeaders.class), response.raw());

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
                    return new BlockBlobsCommitBlockListResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializeHeaders(response.headers(), BlockBlobCommitBlockListHeaders.class));
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

    private BlobDeleteResponse deleteWithRestResponseIntern(String containerName,
                                                            String blobName,
                                                            BlobDeleteOptions options,
                                                            CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        DateTimeRfc1123 ifModifiedSinceConverted = options.getIfModifiedSince() == null ? null : new DateTimeRfc1123(options.getIfModifiedSince());
        DateTimeRfc1123 ifUnmodifiedSinceConverted = options.getIfUnmodifiedSince() == null ? null : new DateTimeRfc1123(options.getIfUnmodifiedSince());

        Call<ResponseBody> call = service.delete(containerName,
            blobName,
            options.getSnapshot(),
            options.getTimeout(),
            options.getLeaseId(),
            options.getDeleteSnapshots(),
            ifModifiedSinceConverted,
            ifUnmodifiedSinceConverted,
            options.getIfMatch(),
            options.getIfNoneMatch(),
            this.getVersion(),
            options.getRequestId());

        ((CancellationTokenImpl) options.getCancellationToken()).registerOnCancel(() -> {
            call.cancel();
        });

        if (callback != null) {
            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 202) {
                            callback.onSuccess(null, deserializeHeaders(response.headers(),
                                BlobDeleteHeaders.class), response.raw());
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
                    return new BlobDeleteResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        deserializeHeaders(response.headers(),
                            BlobDeleteHeaders.class));
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
                                     @Header("If-Modified-Since") String ifModifiedSince,
                                     @Header("If-Unmodified-Since") String ifUnmodifiedSince,
                                     @Header("If-Match") String ifMatch,
                                     @Header("If-None-Match") String ifNoneMatch,
                                     @Header("x-ms-client-request-id") String requestId,
                                     @Header("x-ms-encryption-key") String encryptionKey,
                                     @Header("x-ms-encryption-key-sha256") String encryptionKeySha256,
                                     @Header("x-ms-encryption-algorithm") EncryptionAlgorithmType encryptionAlgorithm);


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
                                      @Body RequestBody blockContent,
                                      @Header("Content-MD5") String transactionalContentMD5,
                                      @Header("x-ms-content-crc64") String transactionalContentCrc64,
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
        Call<ResponseBody> delete(@Path("containerName") String containerName,
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
    }
}
