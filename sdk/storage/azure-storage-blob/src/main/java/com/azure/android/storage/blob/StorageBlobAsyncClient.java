// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.CallbackWithHeader;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.http.interceptor.RequestIdInterceptor;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.core.util.CoreUtil;
import com.azure.android.storage.blob.implementation.util.ModelHelper;
import com.azure.android.storage.blob.interceptor.MetadataInterceptor;
import com.azure.android.storage.blob.interceptor.NormalizeEtagInterceptor;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteHeaders;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobGetTagsHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobSetHttpHeadersHeaders;
import com.azure.android.storage.blob.models.BlobSetMetadataHeaders;
import com.azure.android.storage.blob.models.BlobSetTagsHeaders;
import com.azure.android.storage.blob.models.BlobSetTierHeaders;
import com.azure.android.storage.blob.models.BlobTags;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.ContainerCreateHeaders;
import com.azure.android.storage.blob.models.ContainerDeleteHeaders;
import com.azure.android.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.android.storage.blob.models.ListBlobFlatSegmentHeaders;
import com.azure.android.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.options.BlobDeleteOptions;
import com.azure.android.storage.blob.options.BlobGetPropertiesOptions;
import com.azure.android.storage.blob.options.BlobGetTagsOptions;
import com.azure.android.storage.blob.options.BlobRawDownloadOptions;
import com.azure.android.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.android.storage.blob.options.BlobSetHttpHeadersOptions;
import com.azure.android.storage.blob.options.BlobSetMetadataOptions;
import com.azure.android.storage.blob.options.BlobSetTagsOptions;
import com.azure.android.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.android.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.android.storage.blob.options.ContainerCreateOptions;
import com.azure.android.storage.blob.options.ContainerDeleteOptions;
import com.azure.android.storage.blob.options.ContainerGetPropertiesOptions;
import com.azure.android.storage.blob.transfer.DownloadRequest;
import com.azure.android.storage.blob.transfer.StorageBlobClientMap;
import com.azure.android.storage.blob.transfer.TransferClient;
import com.azure.android.storage.blob.transfer.TransferInfo;
import com.azure.android.storage.blob.transfer.UploadRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private StorageBlobAsyncClient(String id, ServiceClient serviceClient, String serviceVersion, Constraints transferConstraints) {
        this.id = id;
        this.serviceClient = serviceClient;
        this.storageBlobServiceClient = new StorageBlobServiceImpl(this.serviceClient, serviceVersion);
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
     * @param computeMd5    Whether or not the library should calculate the md5 and send it for the service to verify.
     * @param file          The local file to upload.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> upload(Context context,
                                         String containerName,
                                         String blobName,
                                         Boolean computeMd5,
                                         File file) {
        final UploadRequest request = new UploadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .computeMd5(computeMd5)
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
     * @param computeMd5    Whether or not the library should calculate the md5 and send it for the service to verify.
     * @param contentUri    The URI to the Content to upload, the contentUri is resolved using
     *                      {@link android.content.ContentResolver#openAssetFileDescriptor(Uri, String)} with mode as
     *                      "r". The supported URI schemes are: 'content://', 'file://' and 'android.resource://'.
     * @return A LiveData that streams {@link TransferInfo} describing the current state of the transfer.
     */
    public LiveData<TransferInfo> upload(Context context,
                                         String containerName,
                                         String blobName,
                                         Boolean computeMd5,
                                         Uri contentUri) {
        final UploadRequest request = new UploadRequest.Builder()
            .storageClientId(this.id)
            .containerName(containerName)
            .blobName(blobName)
            .computeMd5(computeMd5)
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
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param containerName The container name.
     * @param callback      Callback that receives the response.
     */
    public void createContainer(@NonNull String containerName,
                                @Nullable CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        this.createContainer(new ContainerCreateOptions(containerName), callback);
    }

    /**
     * Creates a new container within a storage account. If a container with the same name already exists, the operation
     * fails.
     * For more information, see the <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * @param options {@link ContainerCreateOptions}
     * @param callback Callback that receives the response.
     */
    public void createContainer(@NonNull ContainerCreateOptions options,
                                @Nullable CallbackWithHeader<Void, ContainerCreateHeaders> callback) {
        Objects.requireNonNull(options);
        storageBlobServiceClient.createContainer(options.getContainerName(), options.getTimeout(),
            options.getMetadata(), options.getPublicAccessType(), options.getCancellationToken(), callback);
    }


    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param containerName The container name.
     * @param callback      Callback that receives the response.
     */
    public void deleteContainer(@NonNull String containerName,
                                @Nullable CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        this.deleteContainer(new ContainerDeleteOptions(containerName), callback);
    }

    /**
     * Marks the specified container for deletion. The container and any blobs contained within it are later deleted
     * during garbage collection. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure Docs</a>.
     *
     * @param options {@link ContainerDeleteOptions}
     * @param callback Callback that receives the response.
     */
    public void deleteContainer(@NonNull ContainerDeleteOptions options,
                                @Nullable CallbackWithHeader<Void, ContainerDeleteHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, true, true, false);
        storageBlobServiceClient.deleteContainer(options.getContainerName(), options.getTimeout(),
            options.getRequestConditions(), options.getCancellationToken(), callback);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param containerName The container name.
     * @param callback      Callback that receives the response.
     */
    public void getContainerProperties(@NonNull String containerName,
                                       @Nullable CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        this.getContainerProperties(new ContainerGetPropertiesOptions(containerName), callback);
    }

    /**
     * Returns the container's metadata and system properties. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-container-metadata">Azure Docs</a>.
     *
     * @param options {@link ContainerGetPropertiesOptions}
     * @param callback Callback that receives the response.
     */
    public void getContainerProperties(@NonNull ContainerGetPropertiesOptions options,
                                       @Nullable CallbackWithHeader<Void, ContainerGetPropertiesHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, true, false);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        storageBlobServiceClient.getContainerProperties(options.getContainerName(), options.getTimeout(),
            requestConditions.getLeaseId(), options.getCancellationToken(), callback);
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
                               Callback<BlobsPage> callback) {
        this.storageBlobServiceClient.listBlobFlatSegment(pageId,
            containerName,
            options,
            new CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders>() {
                @Override
                public void onSuccess(ListBlobsFlatSegmentResponse result, ListBlobFlatSegmentHeaders header, Response response) {
                    List<BlobItem> list = result.getSegment() == null
                        ? new ArrayList<>(0)
                        : result.getSegment().getBlobItems();
                    callback.onSuccess(new BlobsPage(list, pageId, result.getNextMarker()), response);
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    callback.onFailure(throwable, response);
                }
            });
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
     * @param callback          Callback that receives the response.
     * @param cancellationToken The token to request cancellation.
     */
    public void getBlobsInPage(String pageId,
                               String containerName,
                               String prefix,
                               Integer maxResults,
                               List<ListBlobsIncludeItem> include,
                               Integer timeout,
                               CancellationToken cancellationToken,
                               Callback<BlobsPage> callback) {
        this.storageBlobServiceClient.listBlobFlatSegment(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
            cancellationToken,
            new CallbackWithHeader<ListBlobsFlatSegmentResponse, ListBlobFlatSegmentHeaders>() {
                @Override
                public void onSuccess(ListBlobsFlatSegmentResponse result, ListBlobFlatSegmentHeaders header, Response response) {
                    List<BlobItem> list = result.getSegment() == null
                        ? new ArrayList<>(0)
                        : result.getSegment().getBlobItems();
                    callback.onSuccess(new BlobsPage(list, pageId, result.getNextMarker()), response);
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    callback.onFailure(throwable, response);
                }
            });
    }

    /**
     * Returns the blob's metadata and properties.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public void getBlobProperties(@NonNull String containerName, @NonNull String blobName,
                                  @Nullable CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        this.getBlobProperties(new BlobGetPropertiesOptions(containerName, blobName), callback);
    }

    /**
     * Returns the blob's metadata and properties.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>.
     *
     * @param options {@link BlobGetPropertiesOptions}
     * @param callback Callback that receives the response.
     */
    public void getBlobProperties(@NonNull BlobGetPropertiesOptions options,
                                  @Nullable CallbackWithHeader<Void, BlobGetPropertiesHeaders> callback) {
        Objects.requireNonNull(options);
        storageBlobServiceClient.getBlobProperties(options.getContainerName(), options.getBlobName(),
            options.getSnapshot(), options.getTimeout(), options.getRequestConditions(), options.getCpkInfo(),
            options.getCancellationToken(), callback);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param headers       {@link BlobHttpHeaders}
     * @param callback      Callback that receives the response.
     */
    public void setBlobHttpHeaders(@NonNull String containerName,
                                   @NonNull String blobName,
                                   @Nullable BlobHttpHeaders headers,
                                   @Nullable CallbackWithHeader<Void, BlobSetHttpHeadersHeaders> callback) {
        this.setBlobHttpHeaders(new BlobSetHttpHeadersOptions(containerName, blobName, headers), callback);
    }

    /**
     * Changes a blob's HTTP header properties. if only one HTTP header is updated, the others will all be erased. In
     * order to preserve existing values, they must be passed alongside the header being changed.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param options {@link BlobSetHttpHeadersOptions}
     * @param callback Callback that receives the response.
     */
    public void setBlobHttpHeaders(@NonNull BlobSetHttpHeadersOptions options,
                                   @Nullable CallbackWithHeader<Void, BlobSetHttpHeadersHeaders> callback) {
        Objects.requireNonNull(options);
        storageBlobServiceClient.setBlobHttpHeaders(options.getContainerName(), options.getBlobName(),
            options.getTimeout(), options.getRequestConditions(), options.getHeaders(), options.getCancellationToken(),
            callback);
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
     * @param callback      Callback that receives the response.
     */
    public void setBlobMetadata(@NonNull String containerName,
                                @NonNull String blobName,
                                @Nullable Map<String, String> metadata,
                                @Nullable CallbackWithHeader<Void, BlobSetMetadataHeaders> callback) {
        this.setBlobMetadata(new BlobSetMetadataOptions(containerName, blobName, metadata), callback);
    }

    /**
     * Changes a blob's metadata. The specified metadata in this method will replace existing metadata. If old values
     * must be preserved, they must be downloaded and included in the call to this method.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param options {@link BlobSetMetadataOptions}
     * @param callback Callback that receives the response.
     */
    public void setBlobMetadata(@NonNull BlobSetMetadataOptions options,
                                @Nullable CallbackWithHeader<Void, BlobSetMetadataHeaders> callback) {
        Objects.requireNonNull(options);
        storageBlobServiceClient.setBlobMetadata(options.getContainerName(), options.getBlobName(),
            options.getTimeout(), options.getRequestConditions(), options.getMetadata(), options.getCpkInfo(),
            options.getCancellationToken(), callback);
    }

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
     * @param callback      The callback that receives the response.
     */
    public void setBlobAccessTier(@NonNull String containerName,
                                  @NonNull String blobName,
                                  @NonNull AccessTier tier,
                                  @Nullable CallbackWithHeader<Void, BlobSetTierHeaders> callback) {
        this.setBlobAccessTier(new BlobSetAccessTierOptions(containerName, blobName, tier), callback);
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
     * @param callback      The callback that receives the response.
     */
    public void setBlobAccessTier(@NonNull BlobSetAccessTierOptions options,
                                  @Nullable CallbackWithHeader<Void, BlobSetTierHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, true, true);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        storageBlobServiceClient.setBlobTier(options.getContainerName(), options.getBlobName(), options.getAccessTier(),
            options.getSnapshot(), null, /*TODO: (gapra) VersionId?*/ options.getTimeout(),
            options.getRehydratePriority(), requestConditions.getLeaseId(), requestConditions.getTagsConditions(),
            options.getCancellationToken(),callback);
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
     * @param callback      Callback that receives the response.
     */
    public void rawDownload(@NonNull String containerName, @NonNull String blobName,
                            @Nullable CallbackWithHeader<ResponseBody, BlobDownloadHeaders> callback) {
        this.rawDownload(new BlobRawDownloadOptions(containerName, blobName), callback);
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
     * @param callback Callback that receives the response.
     */
    public void rawDownload(@NonNull BlobRawDownloadOptions options,
                            @Nullable CallbackWithHeader<ResponseBody, BlobDownloadHeaders> callback) {
        Objects.requireNonNull(options);
        BlobRange range = options.getRange() == null ? new BlobRange(0) : options.getRange();

        storageBlobServiceClient.rawDownload(options.getContainerName(), options.getBlobName(), options.getSnapshot(),
            options.getTimeout(), range.toHeaderValue(), options.isRetrieveContentRangeMd5(),
            options.isRetrieveContentRangeCrc64(), options.getRequestConditions(), options.getCpkInfo(),
            options.getCancellationToken(), callback);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.</p>
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
    public void stageBlock(@NonNull String containerName, @NonNull String blobName, @NonNull String base64BlockId,
                           @NonNull byte[] blockContent, @Nullable byte[] contentMd5,
                           @Nullable CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        this.stageBlock(new BlockBlobStageBlockOptions(containerName, blobName, base64BlockId, blockContent)
            .setContentMd5(contentMd5), callback);
    }

    /**
     * Uploads the specified block to the block blob's "staging area" to be later committed by a call to
     * commitBlockList.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/put-block">Azure Docs</a>.</p>
     *
     * @param options {@link BlockBlobStageBlockOptions}
     * @param callback          Callback that receives the response.
     */
    public void stageBlock(@NonNull BlockBlobStageBlockOptions options,
                           @Nullable CallbackWithHeader<Void, BlockBlobStageBlockHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, true, false);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        this.storageBlobServiceClient.stageBlock(options.getContainerName(), options.getBlobName(),
            options.getBase64BlockId(), options.getData(), options.getContentMd5(), options.getContentCrc64(),
            options.isComputeMd5(), options.getTimeout(), requestConditions.getLeaseId(), options.getCpkInfo(),
            options.getCancellationToken(), callback);
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobAsyncClient#stageBlock(String, String, String, byte[], byte[], CallbackWithHeader)}. You can
     * call commit Block List to update a blob by uploading only those blocks that have changed, then committing the new
     * and existing blocks together. You can do this by specifying whether to commit a block from the committed block
     * list or from the uncommitted block list, or to commit the most recently uploaded version of the block,
     * whichever list it may belong to.
     *
     * @param containerName  The container name.
     * @param blobName       The blob name.
     * @param base64BlockIds The block IDs.
     * @param overwrite      Indicate whether to overwrite the block list if already exists.
     * @param callback       Callback that receives the response.
     */
    public void commitBlockList(@NonNull String containerName, @NonNull String blobName,
                                @Nullable List<String> base64BlockIds, boolean overwrite,
                                @Nullable CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders> callback) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
        }
        this.commitBlockList(new BlockBlobCommitBlockListOptions(containerName, blobName, base64BlockIds)
                .setRequestConditions(requestConditions), callback);
    }

    /**
     * The Commit Block List operation writes a blob by specifying the list of block IDs that make up the blob.
     * For a block to be written as part of a blob, the block must have been successfully written to the server in a prior
     * {@link StorageBlobAsyncClient#stageBlock(String, String, String, byte[], byte[], CallbackWithHeader)}  operation. You can call
     * commit Block List to update a blob by uploading only those blocks that have changed, then committing the new and existing
     * blocks together. You can do this by specifying whether to commit a block from the committed block list or from
     * the uncommitted block list, or to commit the most recently uploaded version of the block, whichever list it may belong to.
     *
     * @param options {@link BlockBlobCommitBlockListOptions}
     * @param callback Callback that receives the response.
     */
    public void commitBlockList(@NonNull BlockBlobCommitBlockListOptions options,
                                @Nullable CallbackWithHeader<BlockBlobItem, BlockBlobCommitBlockListHeaders> callback) {
        Objects.requireNonNull(options);
        this.storageBlobServiceClient.commitBlockList(options.getContainerName(),
            options.getBlobName(), options.getBase64BlockIds(), options.getContentMd5(), options.getContentCrc64(),
            options.getTimeout(), options.getHeaders(), options.getMetadata(), options.getRequestConditions(),
            options.getCpkInfo(), options.getAccessTier(), options.getCancellationToken(), callback);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public void deleteBlob(@NonNull String containerName, @NonNull String blobName,
                           @Nullable CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        this.deleteBlob(new BlobDeleteOptions(containerName, blobName), callback);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/delete-blob">Azure Docs</a></p>
     *
     * @param options {@link BlobDeleteOptions}
     * @param callback          Callback that receives the response.
     */
    public void deleteBlob(@NonNull BlobDeleteOptions options,
                           @Nullable CallbackWithHeader<Void, BlobDeleteHeaders> callback) {
        Objects.requireNonNull(options);
        storageBlobServiceClient.deleteBlob(options.getContainerName(), options.getBlobName(), options.getSnapshot(),
            options.getTimeout(), options.getDeleteSnapshots(), options.getRequestConditions(),
            options.getCancellationToken(), callback);
    }


    /**
     * Returns the blob's tags.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public void getBlobTags(@NonNull String containerName,
                            @NonNull String blobName,
                            @Nullable CallbackWithHeader<Map<String, String>, BlobGetTagsHeaders> callback) {
        this.getBlobTags(new BlobGetTagsOptions(containerName, blobName), callback);
    }

    /**
     * Returns the blob's tags.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobGetTagsOptions}
     * @param callback          The callback that receives the response.
     */
    public void getBlobTags(@NonNull BlobGetTagsOptions options,
                            @Nullable CallbackWithHeader<Map<String, String>, BlobGetTagsHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, false, true);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        this.storageBlobServiceClient.getTags(options.getContainerName(), options.getBlobName(),
            options.getSnapshot(), null, options.getTimeout(), requestConditions.getTagsConditions(),
            options.getCancellationToken(), callback == null ? null
                : new CallbackWithHeader<BlobTags, BlobGetTagsHeaders>() {
                @Override
                public void onSuccess(BlobTags result, BlobGetTagsHeaders header, Response response) {
                    callback.onSuccess(ModelHelper.populateBlobTags(result), header, response);
                }

                @Override
                public void onFailure(Throwable throwable, Response response) {
                    callback.onFailure(throwable, response);
                }
            });
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values must be
     * preserved, they must be downloaded and included in the call to this method.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param tags          Tags to associate with the blob.
     * @param callback      Callback that receives the response.
     */
    public void setBlobTags(@NonNull String containerName, @NonNull String blobName, @Nullable Map<String, String> tags,
                            @Nullable CallbackWithHeader<Void, BlobSetTagsHeaders> callback) {
        this.setBlobTags(new BlobSetTagsOptions(containerName, blobName, tags), callback);
    }

    /**
     * Sets user defined tags. The specified tags in this method will replace existing tags. If old values must be
     * preserved, they must be downloaded and included in the call to this method.
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-tags">Azure Docs</a></p>
     *
     * @param options {@link BlobSetTagsOptions}
     * @param callback          Callback that receives the response.
     */
    public void setBlobTags(@NonNull BlobSetTagsOptions options,
                            @Nullable CallbackWithHeader<Void, BlobSetTagsHeaders> callback) {
        Objects.requireNonNull(options);
        ModelHelper.validateRequestConditions(options.getRequestConditions(), false, false, false, true);
        BlobRequestConditions requestConditions = options.getRequestConditions() == null ? new BlobRequestConditions()
            : options.getRequestConditions();

        storageBlobServiceClient.setBlobTags(options.getContainerName(), options.getBlobName(),
            options.getTimeout(), null, requestConditions.getTagsConditions(), options.getTags(),
            options.getCancellationToken(), callback);
    }

    /**
     * Builder for {@link StorageBlobAsyncClient}.
     * A builder to configure and build a {@link StorageBlobAsyncClient}.
     */
    public static class Builder {
        private final String storageBlobClientId;
        private BlobServiceVersion serviceVersion;
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
            addStandardInterceptors();
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
         * Sets the service version for the {@link StorageBlobAsyncClient}.
         *
         * @param serviceVersion {@link BlobServiceVersion}
         * @return An updated {@link Builder} with the provided blob service version set.
         */
        public Builder setServiceVersion(BlobServiceVersion serviceVersion) {
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
         *                              transfers to run.
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
            BlobServiceVersion version = this.serviceVersion == null ? BlobServiceVersion.getLatest()
                : this.serviceVersion;
            StorageBlobAsyncClient client = new StorageBlobAsyncClient(this.storageBlobClientId,
                this.serviceClientBuilder.build(), version.getVersion(),
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
