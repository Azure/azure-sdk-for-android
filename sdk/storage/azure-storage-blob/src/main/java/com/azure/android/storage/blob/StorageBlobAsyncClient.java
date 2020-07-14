// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackSimple;
import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.CoreUtil;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDeleteOptions;
import com.azure.android.storage.blob.models.BlobDownloadResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobProperties;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockLookupList;
import com.azure.android.storage.blob.models.CommitBlockListOptions;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.GetBlobPropertiesOptions;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.models.StageBlockOptions;
import com.azure.android.storage.blob.models.StageBlockResult;
import com.azure.android.storage.blob.transfer.DownloadRequest;
import com.azure.android.storage.blob.transfer.StorageBlobClientMap;
import com.azure.android.storage.blob.transfer.TransferClient;
import com.azure.android.storage.blob.transfer.TransferInfo;
import com.azure.android.storage.blob.transfer.UploadRequest;

import java.io.File;
import java.util.List;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Client for Storage Blob service.
 *
 * <p>
 * This client is instantiated through {@link StorageBlobAsyncClient.Builder}.
 */
public class StorageBlobAsyncClient {
    private final String id;
    private final ServiceClient serviceClient;
    private final StorageBlobServiceImpl storageBlobServiceClient;
    private final Constraints transferConstraints;

    private StorageBlobAsyncClient(String id, ServiceClient serviceClient, Constraints transferConstraints) {
        this.id = id;
        this.serviceClient = serviceClient;
        this.storageBlobServiceClient = new StorageBlobServiceImpl(this.serviceClient);
        this.transferConstraints = transferConstraints;
    }

    /**
     * Creates a new {@link Builder} with initial configuration copied from this {@link StorageBlobAsyncClient}.
     *
     * @param storageBlobClientId The unique ID for the new {@link StorageBlobAsyncClient}. This identifier is used to
     *                            associate the {@link StorageBlobAsyncClient} with the upload and download transfers it
     *                            initiates. When a transfer is reloaded from disk (e.g. after an application crash),
     *                            it can only be resumed once a client with the same storageBlobClientId has been
     *                            initialized.
     * @return A new {@link Builder}.
     */
    public StorageBlobAsyncClient.Builder newBuilder(String storageBlobClientId) {
        return new Builder(storageBlobClientId, this);
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
     * Upload the content of a file.
     *
     * @param context       The application context.
     * @param containerName The container to upload the file to.
     * @param blobName      The name of the target blob holding the uploaded file.
     * @param file          The local file to upload.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> upload(Context context,
                                         String containerName,
                                         String blobName, File file) {
        final UploadRequest request = new UploadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .file(file)
            .constraints(this.transferConstraints)
            .build();
        return TransferClient.getInstance(context)
            .upload(request);
    }

    /**
     * Upload content identified by a given URI.
     *
     * @param context       The application context.
     * @param containerName The container to upload the file to.
     * @param blobName      The name of the target blob holding the uploaded file.
     * @param contentUri    The URI to the Content to upload, the contentUri is resolved using
     *                      {@link android.content.ContentResolver#openAssetFileDescriptor(Uri, String)} with mode as
     *                      "r". The supported URI schemes are: 'content://', 'file://' and 'android.resource://'.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> upload(Context context,
                                         String containerName,
                                         String blobName,
                                         Uri contentUri) {
        final UploadRequest request = new UploadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .contentUri(context, contentUri)
            .constraints(this.transferConstraints)
            .build();
        return TransferClient.getInstance(context)
            .upload(request);
    }

    /**
     * Download a blob.
     *
     * @param context       The application context.
     * @param containerName The container to download the blob from.
     * @param blobName      The name of the target blob to download.
     * @param file          The local file to download to.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(Context context,
                                           String containerName,
                                           String blobName,
                                           File file) {
        final DownloadRequest request = new DownloadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .file(file)
            .constraints(this.transferConstraints)
            .build();
        return TransferClient.getInstance(context)
            .download(request);
    }

    /**
     * Download a blob.
     *
     * @param context       The application context.
     * @param containerName The container to download the blob from.
     * @param blobName      The name of the target blob to download.
     * @param contentUri    The URI to the local content where the downloaded blob will be stored.
     * @return LiveData that streams {@link TransferInfo} describing the current state of the download.
     */
    public LiveData<TransferInfo> download(Context context,
                                           String containerName,
                                           String blobName,
                                           Uri contentUri) {
        final DownloadRequest request = new DownloadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .contentUri(context, contentUri)
            .constraints(this.transferConstraints)
            .build();
        return TransferClient.getInstance(context)
            .download(request);
    }

    /**
     * Pause a transfer identified by the given transfer ID. The pause operation is a best-effort, and a transfer
     * that is already executing may continue to transfer.
     * <p>
     * Upon successful scheduling of the pause, any observer observing on {@link LiveData} for this
     * transfer receives a {@link TransferInfo} event with state {@link TransferInfo.State#USER_PAUSED}.
     *
     * @param context    The application context.
     * @param transferId The transfer ID identifies the transfer to pause.
     */
    public void pause(Context context, long transferId) {
        TransferClient.getInstance(context)
            .pause(transferId);
    }

    /**
     * Resume a paused transfer.
     *
     * @param context    The application context
     * @param transferId The transfer ID identifies the transfer to resume.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> resume(Context context, long transferId) {
        return TransferClient.getInstance(context)
            .resume(transferId);
    }

    /**
     * Cancel a transfer identified by the given transfer ID. The cancel operation is a best-effort, and a transfer
     * that is already executing may continue to transfer.
     * <p>
     * Upon successful scheduling of the cancellation, any observer observing on {@link LiveData} for
     * this transfer receives a {@link TransferInfo} event with state {@link TransferInfo.State#CANCELLED}.
     *
     * @param context    The application context.
     * @param transferId The transfer ID identifies the transfer to cancel.
     */
    public void cancel(Context context, long transferId) {
        TransferClient.getInstance(context)
            .cancel(transferId);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options       The page options.
     * @param callback      Callback that receives the retrieved blob list.
     */
    public void getBlobsInPage(String pageId,
                               String containerName,
                               ListBlobsOptions options,
                               Callback<List<BlobItem>> callback) {
        this.storageBlobServiceClient.getBlobsInPage(pageId,
            containerName,
            options,
            callback);
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
     * @param callback          Callback that receives the response.
     * @param cancellationToken The token to request cancellation.
     */
    public void getBlobsInPageWithRestResponse(String pageId,
                                               String containerName,
                                               String prefix,
                                               Integer maxResults,
                                               List<ListBlobsIncludeItem> include,
                                               Integer timeout,
                                               String requestId,
                                               CancellationToken cancellationToken,
                                               Callback<ContainersListBlobFlatSegmentResponse> callback) {
        this.storageBlobServiceClient.getBlobsInPageWithRestResponse(pageId,
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
     * Reads the blob's metadata and properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public void getBlobProperties(String containerName,
                                  String blobName,
                                  CallbackSimple<BlobProperties> callback) {
        storageBlobServiceClient.getBlobProperties(containerName,
            blobName,
            new CallbackWithHeader<Void, BlobGetPropertiesHeaders>() {
                @Override
                public void onSuccess(Void result, BlobGetPropertiesHeaders header, okhttp3.Response response) {
                    callback.onSuccess(ClientUtil.buildBlobProperties(header), response);
                }

                @Override
                public void onFailure(Throwable t, okhttp3.Response response) {
                    callback.onFailure(t, response);
                }
            });
    }

    /**
     * Reads a blob's metadata and properties.
     *
     * @param containerName         The container name.
     * @param blobName              The blob name.
     * @param options               The optional parameter.
     * @param callback              Callback that receives the response.
     */
    public void getBlobProperties(String containerName,
                                  String blobName,
                                  GetBlobPropertiesOptions options,
                                  CallbackSimple<BlobProperties> callback) {
        storageBlobServiceClient.getBlobProperties(containerName,
            blobName,
            ClientUtil.toImplOptions(options),
            new CallbackWithHeader<Void, BlobGetPropertiesHeaders>() {
                @Override
                public void onSuccess(Void result, BlobGetPropertiesHeaders header, okhttp3.Response response) {
                    callback.onSuccess(ClientUtil.buildBlobProperties(header), response);
                }

                @Override
                public void onFailure(Throwable t, okhttp3.Response response) {
                    callback.onFailure(t, response);
                }
            });
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
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public void rawDownload(String containerName,
                            String blobName,
                            Callback<ResponseBody> callback) {
        storageBlobServiceClient.download(containerName,
            blobName,
            callback);
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
     *                              variety of requests to the conditions present. These conditions are entirely optional.
     * @param getRangeContentMd5    When set to true and specified together with the Range, the service returns the
     *                              MD5 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param getRangeContentCrc64  When set to true and specified together with the Range, the service returns the
     *                              CRC64 hash for the range, as long as the range is less than or equal to 4 MB in size.
     * @param version               Specifies the version of the operation to use for this request.
     * @param requestId             Provides a client-generated, opaque value with a 1 KB character limit that is
     *                              recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo               Additional parameters for the operation.
     * @param cancellationToken     The token to request cancellation.
     * @param callback              Callback that receives the response.
     */
    public void rawDownloadWithRestResponse(String containerName,
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
                                            CancellationToken cancellationToken,
                                            Callback<BlobDownloadResponse> callback) {
        range = range == null ? new BlobRange(0) : range;
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        storageBlobServiceClient.downloadWithRestResponse(containerName,
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
            cancellationToken,
            callback);
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
     * @param callback      Callback that receives the response.
     */
    public void stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] blockContent,
                           byte[] contentMd5,
                           CallbackSimple<StageBlockResult> callback) {
        this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            blockContent.length,
            blockContent,
            new com.azure.android.storage.blob.implementation.models.StageBlockOptions().setTransactionalContentMD5(contentMd5),
            new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobStageBlockHeaders header, Response response) {
                    callback.onSuccess(new StageBlockResult(
                        header.getContentMD5(),
                        header.getDateProperty(),
                        header.getXMsContentCrc64(),
                        header.isServerEncrypted(),
                        header.getEncryptionKeySha256()), response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
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
     * @param options           The optional parameters.
     * @param callback          Callback that receives the response.
     */
    public void stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] blockContent,
                           byte[] contentMd5,
                           StageBlockOptions options,
                           CallbackSimple<StageBlockResult> callback) {
        this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            blockContent.length,
            blockContent,
            ClientUtil.toImplOptions(options).setTransactionalContentMD5(contentMd5),
            new CallbackWithHeader<Void, BlockBlobStageBlockHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobStageBlockHeaders header, Response response) {
                    callback.onSuccess(new StageBlockResult(
                        header.getContentMD5(),
                        header.getDateProperty(),
                        header.getXMsContentCrc64(),
                        header.isServerEncrypted(),
                        header.getEncryptionKeySha256()), response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobAsyncClient#stageBlock(String, String, String, byte[], byte[], CallbackSimple)} operation. You can
     * call commit Block List to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. You can do this by specifying whether to commit a block from the committed block
     * list or from the uncommitted block list, or to commit the most recently uploaded version of the block,
     * whichever list it may belong to.
     *
     * @param containerName  The container name.
     * @param blobName       The blob name.
     * @param base64BlockIds The block IDs.
     * @param callback       Callback that receives the response.
     */
    public void commitBlockList(String containerName,
                                String blobName,
                                List<String> base64BlockIds,
                                CallbackSimple<BlockBlobItem> callback) {
        this.storageBlobServiceClient.commitBlockList(containerName,
            blobName,
            new BlockLookupList().setCommitted(base64BlockIds),
            new com.azure.android.storage.blob.implementation.models.CommitBlockListOptions(),
            new CallbackWithHeader<Void, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobCommitBlockListHeaders header, Response response) {
                    callback.onSuccess(ClientUtil.buildBlockBlobItem(header), response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobAsyncClient#stageBlock(String, String, String, byte[], byte[], StageBlockOptions, CallbackSimple)} operation.
     * You can call commit Block List to update a blob by uploading only those blocks that have changed, then committing
     * the new and existing blocks together. You can do this by specifying whether to commit a block from the committed block
     * list or from the uncommitted block list, or to commit the most recently uploaded version of the block, whichever list it may belong to.
     *
     * @param containerName     The container name.
     * @param blobName          The blob name.
     * @param base64BlockIds    The block IDs.
     * @param options           The optional parameter.
     * @param callback          Callback that receives the response.
     */
    public void commitBlockList(String containerName,
                                String blobName,
                                List<String> base64BlockIds,
                                CommitBlockListOptions options,
                                CallbackSimple<BlockBlobItem> callback) {
        this.storageBlobServiceClient.commitBlockList(containerName,
            blobName,
            new BlockLookupList().setCommitted(base64BlockIds),
            ClientUtil.toImplOptions(options),
            new CallbackWithHeader<Void, BlockBlobCommitBlockListHeaders>() {
                @Override
                public void onSuccess(Void result, BlockBlobCommitBlockListHeaders header, Response response) {
                    callback.onSuccess(ClientUtil.buildBlockBlobItem(header), response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    void delete(String containerName,
                String blobName,
                CallbackSimple<Void> callback) {
        storageBlobServiceClient.delete(containerName,
            blobName,
            new com.azure.android.storage.blob.implementation.models.BlobDeleteOptions(),
            new CallbackWithHeader<Void, BlobDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, BlobDeleteHeaders header, Response response) {
                    callback.onSuccess(result, response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
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
     */
    public void delete(String containerName,
                String blobName,
                BlobDeleteOptions options,
                CallbackSimple<Void> callback) {
        storageBlobServiceClient.delete(containerName,
            blobName,
            ClientUtil.toImplOptions(options),
            new CallbackWithHeader<Void, BlobDeleteHeaders>() {
                @Override
                public void onSuccess(Void result, BlobDeleteHeaders header, Response response) {
                    callback.onSuccess(result, response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
    }

    /**
     * Builder for {@link StorageBlobAsyncClient}.
     * A builder to configure and build a {@link StorageBlobAsyncClient}.
     */
    public static class Builder {
        private final String storageBlobClientId;
        private final ServiceClient.Builder serviceClientBuilder;
        private final Constraints.Builder transferConstraintsBuilder;
        private static final StorageBlobClientMap STORAGE_BLOB_CLIENTS;

        static {
            STORAGE_BLOB_CLIENTS = StorageBlobClientMap.getInstance();
        }

        /**
         * Creates a {@link Builder}.
         *
         * @param storageBlobClientId The unique ID for the {@link StorageBlobAsyncClient} this builder builds. This
         *                            identifier is used to associate this {@link StorageBlobAsyncClient} with the upload and
         *                            download transfers it initiates. When a transfer is reloaded from disk (e.g.
         *                            after an application crash), it can only be resumed once a client with the same
         *                            storageBlobClientId has been initialized.
         */
        public Builder(String storageBlobClientId) {
            this(storageBlobClientId, new ServiceClient.Builder());
            this.serviceClientBuilder
                .addInterceptor(new AddDateInterceptor())
                .setSerializationFormat(SerializerFormat.XML);
        }

        /**
         * Creates a {@link Builder} that uses the provided {@link com.azure.android.core.http.ServiceClient.Builder}
         * to build a {@link ServiceClient} for the {@link StorageBlobAsyncClient}.
         *
         * <p>
         * The builder produced {@link ServiceClient} is used by the {@link StorageBlobAsyncClient} to make Rest API calls.
         * Multiple {@link StorageBlobAsyncClient} instances can share the same {@link ServiceClient} instance, for e.g.
         * when a new {@link StorageBlobAsyncClient} is created from an existing {@link StorageBlobAsyncClient} through
         * {@link StorageBlobAsyncClient#newBuilder(String)} ()} then both shares the same {@link ServiceClient}.
         * The {@link ServiceClient} composes HttpClient, HTTP settings (such as connection timeout, interceptors)
         * and Retrofit for Rest calls.
         *
         * @param storageBlobClientId  The unique ID for the {@link StorageBlobAsyncClient} this builder builds.
         * @param serviceClientBuilder The {@link com.azure.android.core.http.ServiceClient.Builder}.
         */
        public Builder(String storageBlobClientId, ServiceClient.Builder serviceClientBuilder) {
            this(storageBlobClientId, serviceClientBuilder, new Constraints.Builder());
            this.transferConstraintsBuilder
                .setRequiredNetworkType(NetworkType.CONNECTED);
        }

        private Builder(String storageBlobClientId,
                        ServiceClient.Builder serviceClientBuilder,
                        Constraints.Builder transferConstraintsBuilder) {
            if (CoreUtil.isNullOrEmpty(storageBlobClientId)) {
                throw new IllegalArgumentException("'storageBlobClientId' cannot be null or empty.");
            }
            if (Builder.STORAGE_BLOB_CLIENTS.contains(storageBlobClientId)) {
                throw new IllegalArgumentException("A StorageBlobClient with id '" + storageBlobClientId + "' already exists.");
            }
            this.storageBlobClientId = storageBlobClientId;
            this.serviceClientBuilder
                = Objects.requireNonNull(serviceClientBuilder, "serviceClientBuilder cannot be null.");
            this.transferConstraintsBuilder
                = Objects.requireNonNull(transferConstraintsBuilder, "transferConstraintsBuilder cannot be null.");
        }

        /**
         * Sets the base URL for the {@link StorageBlobAsyncClient}.
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
         * Sets whether device should be charging for running the transfers. The default value is {@code false}.
         *
         * @param requiresCharging {@code true} if the device must be charging for the transfer to run.
         * @return An updated {@link Builder} with the provided charging requirement set.
         */
        public Builder setTransferRequiresCharging(boolean requiresCharging) {
            this.transferConstraintsBuilder.setRequiresCharging(requiresCharging);
            return this;
        }

        /**
         * Sets whether device should be idle for running the transfers. The default value is {@code false}.
         *
         * @param requiresDeviceIdle {@code true} if the device must be idle for transfers to run.
         * @return An updated {@link Builder} with the provided setting set.
         */
        @RequiresApi(23)
        public Builder setTransferRequiresDeviceIdle(boolean requiresDeviceIdle) {
            if (Build.VERSION.SDK_INT >= 23) {
                this.transferConstraintsBuilder.setRequiresDeviceIdle(requiresDeviceIdle);
            }
            return this;
        }

        /**
         * Sets the particular {@link NetworkType} the device should be in for running the transfers.
         * <p>
         * The default network type that {@link TransferClient} uses is {@link NetworkType#CONNECTED}.
         *
         * @param networkType The type of network required for transfers to run.
         * @return An updated {@link Builder} with the provided network type set.
         */
        public Builder setTransferRequiredNetworkType(@NonNull NetworkType networkType) {
            Objects.requireNonNull(networkType, "'networkType' cannot be null.");
            if (networkType == NetworkType.NOT_REQUIRED) {
                throw new IllegalArgumentException(
                    "The network type NOT_REQUIRED is not a valid transfer configuration.");
            }
            this.transferConstraintsBuilder.setRequiredNetworkType(networkType);
            return this;
        }

        /**
         * Sets whether device battery should be at an acceptable level for running the transfers. The default value
         * is {@code false}.
         *
         * @param requiresBatteryNotLow {@code true} if the battery should be at an acceptable level for the
         *                                          transfers to run.
         * @return An updated {@link Builder} with the provided battery requirement set.
         */
        public Builder setTransferRequiresBatteryNotLow(boolean requiresBatteryNotLow) {
            this.transferConstraintsBuilder.setRequiresBatteryNotLow(requiresBatteryNotLow);
            return this;
        }

        /**
         * Sets whether the device's available storage should be at an acceptable level for running
         * the transfers. The default value is {@code false}.
         *
         * @param requiresStorageNotLow {@code true} if the available storage should not be below a
         *                              a critical threshold for the transfer to run.
         * @return An updated {@link Builder} with the provided storage requirement set.
         */
        public Builder setTransferRequiresStorageNotLow(boolean requiresStorageNotLow) {
            this.transferConstraintsBuilder.setRequiresStorageNotLow(requiresStorageNotLow);
            return this;
        }

        /**
         * Builds a {@link StorageBlobAsyncClient} based on this {@link Builder}'s configuration.
         *
         * @return A {@link StorageBlobAsyncClient}.
         */
        public StorageBlobAsyncClient build() {
            Constraints transferConstraints = this.transferConstraintsBuilder.build();
            NetworkType networkType = transferConstraints.getRequiredNetworkType();
            if (networkType == null || networkType == NetworkType.NOT_REQUIRED) {
                throw new IllegalArgumentException(
                    "The null or NOT_REQUIRED NetworkType is not a valid transfer configuration.");
            }
            StorageBlobAsyncClient client = new StorageBlobAsyncClient(this.storageBlobClientId,
                this.serviceClientBuilder.build(),
                transferConstraints);
            Builder.STORAGE_BLOB_CLIENTS.add(storageBlobClientId, client);
            return client;
        }

        private Builder(String storageBlobClientId, final StorageBlobAsyncClient storageBlobAsyncClient) {
            this(storageBlobClientId,
                storageBlobAsyncClient.serviceClient.newBuilder(),
                newBuilder(storageBlobAsyncClient.transferConstraints));
        }

        private static androidx.work.Constraints.Builder newBuilder(androidx.work.Constraints constraints) {
            Constraints.Builder builder = new Constraints.Builder();
            builder.setRequiresCharging(constraints.requiresCharging());
            if (Build.VERSION.SDK_INT >= 23) {
                builder.setRequiresDeviceIdle(constraints.requiresDeviceIdle());
            }
            builder.setRequiredNetworkType(constraints.getRequiredNetworkType());
            builder.setRequiresBatteryNotLow(constraints.requiresBatteryNotLow());
            builder.setRequiresStorageNotLow(constraints.requiresStorageNotLow());
            return builder;
        }
    }
}
