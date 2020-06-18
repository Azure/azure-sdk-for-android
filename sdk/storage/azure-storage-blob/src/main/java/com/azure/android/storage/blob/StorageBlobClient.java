// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceCall;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.http.interceptor.AddDateInterceptor;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDeleteResponse;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.android.storage.blob.models.BlobGetPropertiesHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRange;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobGetPropertiesResponse;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import org.threeten.bp.OffsetDateTime;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.ResponseBody;

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
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options       The page options.
     * @return A list of blobs.
     */
    public List<BlobItem> getBlobsInPage(String pageId,
                                         String containerName,
                                         ListBlobsOptions options) {
        return this.storageBlobServiceClient.getBlobsInPage(pageId,
            containerName,
            options);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param options       The page options.
     * @param callback      Callback that receives the retrieved blob list.
     */
    public ServiceCall getBlobsInPage(String pageId,
                                      String containerName,
                                      ListBlobsOptions options,
                                      Callback<List<BlobItem>> callback) {
        return this.storageBlobServiceClient.getBlobsInPage(pageId,
            containerName,
            options,
            callback);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param prefix        Filters the results to return only blobs whose name begins with the specified prefix.
     * @param maxResults    Specifies the maximum number of blobs to return.
     * @param include       Include this parameter to specify one or more datasets to include in the response.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see
     *                      &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in
     *                      the analytics logs when storage analytics logging is enabled.
     * @return A response object containing a list of blobs.
     */
    public ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponse(String pageId,
                                                                                String containerName,
                                                                                String prefix,
                                                                                Integer maxResults,
                                                                                List<ListBlobsIncludeItem> include,
                                                                                Integer timeout,
                                                                                String requestId) {
        return this.storageBlobServiceClient.getBlobsInPageWithRestResponse(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId);
    }

    /**
     * Gets a list of blobs identified by a page id in a given container.
     *
     * @param pageId        Identifies the portion of the list to be returned.
     * @param containerName The container name.
     * @param prefix        Filters the results to return only blobs whose name begins with the specified prefix.
     * @param maxResults    Specifies the maximum number of blobs to return.
     * @param include       Include this parameter to specify one or more datasets to include in the response.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see
     *                      &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in
     *                      the analytics logs when storage analytics logging is enabled.
     * @param callback      Callback that receives the response.
     */
    public ServiceCall getBlobsInPageWithRestResponse(String pageId,
                                                      String containerName,
                                                      String prefix,
                                                      Integer maxResults,
                                                      List<ListBlobsIncludeItem> include,
                                                      Integer timeout,
                                                      String requestId,
                                                      Callback<ContainersListBlobFlatSegmentResponse> callback) {
        return this.storageBlobServiceClient.getBlobsInPageWithRestResponse(pageId,
            containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            callback);
    }

    /**
     * Reads the blob's metadata & properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    public BlobGetPropertiesHeaders getBlobProperties(String containerName,
                                                      String blobName) {
        return storageBlobServiceClient.getBlobProperties(containerName,  blobName);
    }

    /**
     * Reads the blob's metadata & properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public ServiceCall getBlobProperties(String containerName,
                                         String blobName,
                                         Callback<BlobGetPropertiesHeaders> callback) {
        return storageBlobServiceClient.getBlobProperties(containerName,
            blobName,
            callback);
    }

    /**
     * Reads a blob's metadata & properties.
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
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobGetPropertiesResponse getBlobPropertiesWithRestResponse(String containerName,
                                                                       String blobName,
                                                                       String snapshot,
                                                                       Integer timeout,
                                                                       String version,
                                                                       BlobRequestConditions blobRequestConditions,
                                                                       String requestId,
                                                                       CpkInfo cpkInfo) {
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            blobRequestConditions.getLeaseId(),
            requestId,
            cpkInfo);
    }

    /**
     * Reads a blob's metadata & properties.
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
     */
    public ServiceCall getBlobPropertiesWithRestResponse(String containerName,
                                                         String blobName,
                                                         String snapshot,
                                                         Integer timeout,
                                                         String version,
                                                         BlobRequestConditions blobRequestConditions,
                                                         String requestId,
                                                         CpkInfo cpkInfo,
                                                         Callback<BlobGetPropertiesResponse> callback) {
        blobRequestConditions = blobRequestConditions == null ? new BlobRequestConditions() : blobRequestConditions;

        return storageBlobServiceClient.getBlobPropertiesWithRestResponse(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            blobRequestConditions.getLeaseId(),
            requestId,
            cpkInfo,
            callback);
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    public ResponseBody download(String containerName,
                                 String blobName) {
        return storageBlobServiceClient.download(containerName,
            blobName);
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    public ServiceCall download(String containerName,
                                String blobName,
                                Callback<ResponseBody> callback) {
        return storageBlobServiceClient.download(containerName,
            blobName,
            callback);
    }

    /**
     * Reads a range of bytes from a blob.
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
     * @return The response information returned from the server when downloading a blob.
     */
    public BlobDownloadAsyncResponse downloadWithRestResponse(String containerName,
                                                              String blobName,
                                                              String snapshot,
                                                              Integer timeout,
                                                              BlobRange range,
                                                              BlobRequestConditions blobRequestConditions,
                                                              Boolean getRangeContentMd5,
                                                              Boolean getRangeContentCrc64,
                                                              String version,
                                                              String requestId,
                                                              CpkInfo cpkInfo) {
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
            cpkInfo);
    }

    /**
     * Reads a range of bytes from a blob.
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
     */
    public ServiceCall downloadWithRestResponse(String containerName,
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
                                                Callback<BlobDownloadAsyncResponse> callback) {
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
            callback);
    }

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

    public ServiceCall stageBlock(String containerName,
                                  String blobName,
                                  String base64BlockId,
                                  byte[] blockContent,
                                  byte[] contentMd5,
                                  Callback<Void> callback) {
        return this.storageBlobServiceClient.stageBlock(containerName,
            blobName,
            base64BlockId,
            blockContent,
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

    public ServiceCall stageBlockWithRestResponse(String containerName,
                                                  String blobName,
                                                  String base64BlockId,
                                                  byte[] body,
                                                  byte[] transactionalContentMD5,
                                                  byte[] transactionalContentCrc64,
                                                  Integer timeout,
                                                  String leaseId,
                                                  String requestId,
                                                  CpkInfo cpkInfo,
                                                  Callback<BlockBlobsStageBlockResponse> callback) {
        return this.storageBlobServiceClient.stageBlockWithRestResponse(containerName,
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

    public ServiceCall commitBlockList(String containerName,
                                       String blobName,
                                       List<String> base64BlockIds,
                                       boolean overwrite,
                                       Callback<BlockBlobItem> callBack) {
        return this.storageBlobServiceClient.commitBlockList(containerName,
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

    public ServiceCall commitBlockListWithRestResponse(String containerName,
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
                                                       Callback<BlockBlobsCommitBlockListResponse> callback) {
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
            tier, callback);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     */
    Void delete(String containerName,
                String blobName) {
        return storageBlobServiceClient.delete(containerName,
            blobName);
    }

    /**
     * Deletes the specified blob or snapshot. Note that deleting a blob also deletes all its snapshots.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     * @return A handle to the service call.
     */
    ServiceCall delete(String containerName,
                       String blobName,
                       Callback<Void> callback) {
        return storageBlobServiceClient.delete(containerName,
            blobName,
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
    BlobDeleteResponse deleteWithResponse(String containerName,
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
                                          String requestId) {
        return storageBlobServiceClient.deleteWithResponse(containerName,
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
            requestId);
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
    ServiceCall deleteWithResponse(String containerName,
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
                                   Callback<BlobDeleteResponse> callback) {
        return storageBlobServiceClient.deleteWithResponse(containerName,
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
            callback);
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
