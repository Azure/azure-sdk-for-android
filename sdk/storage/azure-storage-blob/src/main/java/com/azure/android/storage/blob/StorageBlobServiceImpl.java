// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceCall;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.Base64Util;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.storage.blob.models.AccessTier;
import com.azure.android.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.android.storage.blob.models.BlobDownloadHeaders;
import com.azure.android.storage.blob.models.BlobHttpHeaders;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobRequestConditions;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.BlockBlobCommitBlockListHeaders;
import com.azure.android.storage.blob.models.BlockBlobItem;
import com.azure.android.storage.blob.models.BlockBlobStageBlockHeaders;
import com.azure.android.storage.blob.models.BlockBlobsCommitBlockListResponse;
import com.azure.android.storage.blob.models.BlockBlobsStageBlockResponse;
import com.azure.android.storage.blob.models.BlockLookupList;
import com.azure.android.storage.blob.models.ContainerListBlobFlatSegmentHeaders;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.CpkInfo;
import com.azure.android.storage.blob.models.EncryptionAlgorithmType;
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

    List<BlobItem> getBlobsInPage(String pageId,
                                  String containerName,
                                  ListBlobsOptions options) {
        options = options == null ? new ListBlobsOptions() : options;
        ContainersListBlobFlatSegmentResponse response
            = this.getBlobsInPageWithRestResponse(pageId, containerName, options.getPrefix(),
            options.getMaxResultsPerPage(), options.getDetails().toList(),
            null, null);
        List<BlobItem> value = response.getValue().getSegment() == null
            ? new ArrayList<>(0)
            : response.getValue().getSegment().getBlobItems();
        return value;
    }

    void getBlobsInPage(String pageId,
                        String containerName,
                        ListBlobsOptions options,
                        Callback<List<BlobItem>> callback) {
        options = options == null ? new ListBlobsOptions() : options;
        this.getBlobsInPageWithRestResponse(pageId, containerName, options.getPrefix(),
            options.getMaxResultsPerPage(), options.getDetails().toList(),
            null, null, new Callback<ContainersListBlobFlatSegmentResponse>() {
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
                                                                         String requestId) {
        return this.getBlobsInPageWithRestResponseIntern(pageId, containerName,
            prefix,
            maxResults,
            include,
            timeout,
            requestId,
            null);
    }

    void getBlobsInPageWithRestResponse(String pageId,
                                        String containerName,
                                        String prefix,
                                        Integer maxResults,
                                        List<ListBlobsIncludeItem> include,
                                        Integer timeout,
                                        String requestId,
                                        Callback<ContainersListBlobFlatSegmentResponse> callback) {
        this.getBlobsInPageWithRestResponseIntern(pageId,
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
     * @return The blob's metadata.
     */
    BlobDownloadHeaders getBlobProperties(String containerName,
                                          String blobName) {
        return getBlobPropertiesWithHeaders(containerName,
            blobName,
            null,
            null,
            null,
            null,
            null,
            null);
    }

    /**
     * Reads the blob's metadata & properties.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    ServiceCall getBlobProperties(String containerName,
                                  String blobName,
                                  Callback<BlobDownloadHeaders> callback) {
        return getBlobPropertiesWithHeaders(containerName,
            blobName,
            null,
            null,
            null,
            null,
            null,
            null,
            callback);
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param snapshot      he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo       Additional parameters for the operation.
     * @return A response containing the blob metadata.
     */
    BlobDownloadHeaders getBlobPropertiesWithHeaders(String containerName,
                                                     String blobName,
                                                     String snapshot,
                                                     Integer timeout,
                                                     String version,
                                                     String leaseId,
                                                     String requestId,
                                                     CpkInfo cpkInfo) {
        return getBlobPropertiesWithHeadersIntern(containerName,
            blobName,
            snapshot,
            timeout,
            version,
            leaseId,
            requestId,
            cpkInfo,
            null).getResult();
    }

    /**
     * The Get Blob Properties operation reads a blob's metadata and properties. You can also call it to read a
     * snapshot or version.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param snapshot      he snapshot parameter is an opaque DateTime value that, when present, specifies the blob snapshot to retrieve. For more information on working with blob snapshots, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/creating-a-snapshot-of-a-blob"&gt;Creating a Snapshot of a Blob.&lt;/a&gt;.
     * @param timeout       The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param version       Specifies the version of the operation to use for this request.
     * @param leaseId       If specified, the operation only succeeds if the resource's lease is active and matches this ID.
     * @param requestId     Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param cpkInfo       Additional parameters for the operation.
     * @param callback      Callback that receives the response.
     */
    ServiceCall getBlobPropertiesWithHeaders(String containerName,
                                             String blobName,
                                             String snapshot,
                                             Integer timeout,
                                             String version,
                                             String leaseId,
                                             String requestId,
                                             CpkInfo cpkInfo,
                                             Callback<BlobDownloadHeaders> callback) {
        CallAndOptionalResult<BlobDownloadHeaders> callAndOptionalResult =
            this.getBlobPropertiesWithHeadersIntern(containerName,
                blobName,
                snapshot,
                timeout,
                version,
                leaseId,
                requestId,
                cpkInfo,
                callback);

        return new ServiceCall(callAndOptionalResult.getCall());
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @return A response containing the blob data.
     */
    BlobDownloadAsyncResponse download(String containerName,
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
            null);
    }

    /**
     * Reads the entire blob.
     *
     * @param containerName The container name.
     * @param blobName      The blob name.
     * @param callback      Callback that receives the response.
     */
    ServiceCall download(String containerName,
                         String blobName,
                         Callback<BlobDownloadAsyncResponse> callback) {
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
            callback);
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
    BlobDownloadAsyncResponse downloadWithRestResponse(String containerName,
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
                                                       CpkInfo cpkInfo) {
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
            null).getResult();
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
    ServiceCall downloadWithRestResponse(String containerName,
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
                                         Callback<BlobDownloadAsyncResponse> callback) {
        CallAndOptionalResult<BlobDownloadAsyncResponse> callAndOptionalResult = this.downloadWithRestResponseIntern(containerName,
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
            callback);

        return new ServiceCall(callAndOptionalResult.getCall());
    }

    ServiceCall stageBlock(String containerName,
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
            null);
    }

    ServiceCall stageBlock(String containerName,
                           String blobName,
                           String base64BlockId,
                           byte[] blockContent,
                           byte[] contentMd5,
                           Callback<Void> callback) {
        return this.stageBlockWithRestResponse(containerName,
            blobName,
            base64BlockId,
            blockContent,
            contentMd5,
            null,
            null,
            null,
            null,
            null, new Callback<BlockBlobsStageBlockResponse>() {
                @Override
                public void onResponse(BlockBlobsStageBlockResponse response) {
                    callback.onResponse(null);
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }
            });
    }

    BlockBlobsStageBlockResponse stageBlockWithRestResponse(String containerName,
                                                            String blobName,
                                                            String base64BlockId,
                                                            byte[] blockContent,
                                                            byte[] transactionalContentMD5,
                                                            byte[] transactionalContentCrc64,
                                                            Integer timeout,
                                                            String leaseId,
                                                            String requestId,
                                                            CpkInfo cpkInfo) {
        return this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            null).getResult();
    }

    ServiceCall stageBlockWithRestResponse(String containerName,
                                           String blobName,
                                           String base64BlockId,
                                           byte[] blockContent,
                                           byte[] transactionalContentMD5,
                                           byte[] transactionalContentCrc64,
                                           Integer timeout,
                                           String leaseId,
                                           String requestId,
                                           CpkInfo cpkInfo,
                                           Callback<BlockBlobsStageBlockResponse> callback) {
        CallAndOptionalResult<BlockBlobsStageBlockResponse> r = this.stageBlockWithRestResponseIntern(containerName,
            blobName,
            base64BlockId,
            blockContent,
            transactionalContentMD5,
            transactionalContentCrc64,
            timeout,
            leaseId,
            requestId,
            cpkInfo,
            callback);
        return new ServiceCall(r.getCall());
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
            null);
        return response.getBlockBlobItem();
    }

    ServiceCall commitBlockList(String containerName,
                                String blobName,
                                List<String> base64BlockIds,
                                boolean overwrite,
                                Callback<BlockBlobItem> callBack) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch("*");
        }
        return this.commitBlockListWithRestResponse(containerName,
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
            null, new Callback<BlockBlobsCommitBlockListResponse>() {
                @Override
                public void onResponse(BlockBlobsCommitBlockListResponse response) {
                    callBack.onResponse(response.getBlockBlobItem());
                }

                @Override
                public void onFailure(Throwable t) {
                    callBack.onFailure(t);
                }
            });
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
                                                                      String requestId,
                                                                      CpkInfo cpkInfo,
                                                                      AccessTier tier) {
        return this.commitBlockListIntern(containerName,
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
            tier,
            null).getResult();
    }

    ServiceCall commitBlockListWithRestResponse(String containerName,
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
        CallAndOptionalResult<BlockBlobsCommitBlockListResponse> r = this.commitBlockListIntern(containerName,
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
            tier,
            callback);
        return new ServiceCall(r.getCall());
    }

    private ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponseIntern(String pageId,
                                                                                       String containerName,
                                                                                       String prefix,
                                                                                       Integer maxResults,
                                                                                       List<ListBlobsIncludeItem> include,
                                                                                       Integer timeout,
                                                                                       String requestId,
                                                                                       Callback<ContainersListBlobFlatSegmentResponse> callback) {
        final String resType = "container";
        final String comp = "list";
        if (callback != null) {
            executeCall(service.listBlobFlatSegment(containerName,
                prefix,
                pageId,
                maxResults,
                this.serializerAdapter.serializeList(include, SerializerAdapter.CollectionFormat.CSV),
                timeout,
                XMS_VERSION,
                requestId,
                resType,
                comp
                /*context*/), new retrofit2.Callback<ResponseBody>() {
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
            Response<ResponseBody> response = executeCall(service.listBlobFlatSegment(containerName,
                prefix,
                pageId,
                maxResults,
                this.serializerAdapter.serializeList(include, SerializerAdapter.CollectionFormat.CSV),
                timeout,
                XMS_VERSION,
                requestId,
                resType,
                comp
                /*context*/));

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    ListBlobsFlatSegmentResponse typedContent = deserializeContent(response.body(),
                        ListBlobsFlatSegmentResponse.class);
                    ContainerListBlobFlatSegmentHeaders typedHeader = deserializeHeaders(response.headers(),
                        ContainerListBlobFlatSegmentHeaders.class);

                    return new ContainersListBlobFlatSegmentResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        typedContent,
                        typedHeader);
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

    private CallAndOptionalResult<BlobDownloadHeaders> getBlobPropertiesWithHeadersIntern(String containerName,
                                                                                          String blobName,
                                                                                          String snapshot,
                                                                                          Integer timeout,
                                                                                          String version,
                                                                                          String leaseId,
                                                                                          String requestId,
                                                                                          CpkInfo cpkInfo,
                                                                                          Callback<BlobDownloadHeaders> callback) {
        String encryptionKey = null;
        String encryptionKeySha256 = null;
        EncryptionAlgorithmType encryptionAlgorithm = null;

        if (cpkInfo != null) {
            encryptionKey = cpkInfo.getEncryptionKey();
            encryptionKeySha256 = cpkInfo.getEncryptionKeySha256();
            encryptionAlgorithm = cpkInfo.getEncryptionAlgorithm();
        }

        if (callback != null) {
            Call<Void> call = service.getBlobProperties(containerName,
                blobName,
                snapshot,
                timeout,
                XMS_VERSION, // TODO: Replace with 'version'.
                leaseId,
                requestId,
                encryptionKey,
                encryptionKeySha256,
                encryptionAlgorithm);

            executeCall(call, new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 200) {

                            callback.onResponse(deserializeHeaders(response.headers(), BlobDownloadHeaders.class));
                        } else { // Error response
                            callback.onFailure(
                                new BlobStorageException("Response failed with error code: " + response.code(),
                                    response.raw()));
                        }
                    } else { // Error response
                        callback.onFailure(
                            new BlobStorageException("Response failed with error code: " + response.code(),
                                response.raw()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    callback.onFailure(t);
                }
            });

            return new CallAndOptionalResult<>(call, null);
        } else {
            Call<Void> call = service.getBlobProperties(containerName,
                blobName,
                snapshot,
                timeout,
                XMS_VERSION, // TODO: Replace with 'version'.
                leaseId,
                requestId,
                encryptionKey,
                encryptionKeySha256,
                encryptionAlgorithm);

            Response<Void> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    BlobDownloadHeaders result = deserializeHeaders(response.headers(), BlobDownloadHeaders.class);

                    return new CallAndOptionalResult<>(call, result);
                } else { // Error response
                    throw new BlobStorageException("Response failed with error code: " + response.code(),
                        response.raw());
                }
            } else { // Error response
                throw new BlobStorageException("Response failed with error code: " + response.code(), response.raw());
            }
        }
    }

    private CallAndOptionalResult<BlobDownloadAsyncResponse> downloadWithRestResponseIntern(String containerName,
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
                                                                                            Callback<BlobDownloadAsyncResponse> callback) {
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

        if (callback != null) {
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

            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() >= 200 && response.code() < 300) {
                            BlobDownloadHeaders typedHeaders = deserializeHeaders(response.headers(),
                                BlobDownloadHeaders.class);

                            callback.onResponse(new BlobDownloadAsyncResponse(response.raw().request(),
                                response.code(),
                                response.headers(),
                                response.body(),
                                typedHeaders));
                        } else { // Error response
                            String strContent = readAsString(response.body());

                            callback.onFailure(new BlobStorageException(strContent, response.raw()));
                        }
                    } else { // Error response
                        String strContent = readAsString(response.errorBody());

                        callback.onFailure(new BlobStorageException(strContent, response.raw()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    callback.onFailure(t);
                }
            });

            return new CallAndOptionalResult<>(call, null);
        } else {
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

            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 200) {
                    BlobDownloadHeaders headers = deserializeHeaders(response.headers(), BlobDownloadHeaders.class);

                    BlobDownloadAsyncResponse result = new BlobDownloadAsyncResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        response.body(),
                        headers);

                    return new CallAndOptionalResult<>(call, result);
                } else { // Error response
                    String strContent = readAsString(response.body());

                    throw new BlobStorageException(strContent, response.raw());
                }
            } else { // Error response
                String strContent = readAsString(response.errorBody());

                throw new BlobStorageException(strContent, response.raw());
            }
        }
    }

    private CallAndOptionalResult<BlockBlobsStageBlockResponse> stageBlockWithRestResponseIntern(String containerName,
                                                                                                 String blobName,
                                                                                                 String base64BlockId,
                                                                                                 byte[] blockContent,
                                                                                                 byte[] transactionalContentMD5,
                                                                                                 byte[] transactionalContentCrc64,
                                                                                                 Integer timeout,
                                                                                                 String leaseId,
                                                                                                 String requestId,
                                                                                                 CpkInfo cpkInfo,
                                                                                                 Callback<BlockBlobsStageBlockResponse> callback) {
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
        String transactionalContentMD5Converted = Base64Util.encodeToString(transactionalContentMD5);
        String transactionalContentCrc64Converted = Base64Util.encodeToString(transactionalContentCrc64);
        //
        int contentLength = blockContent.length;
        RequestBody body = RequestBody.create(MediaType.get("application/octet-stream"), blockContent);

        if (callback != null) {
            Call<ResponseBody> call = service.stageBlock(containerName,
                blobName,
                base64BlockId,
                contentLength,
                transactionalContentMD5Converted,
                transactionalContentCrc64Converted,
                body,
                timeout,
                leaseId,
                XMS_VERSION,
                requestId,
                comp,
                encryptionKey,
                encryptionKeySha256,
                encryptionAlgorithm);

            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            BlockBlobStageBlockHeaders typedHeader = deserializeHeaders(response.headers(),
                                BlockBlobStageBlockHeaders.class);
                            callback.onResponse(new BlockBlobsStageBlockResponse(response.raw().request(),
                                response.code(),
                                response.headers(),
                                null,
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
            return new CallAndOptionalResult(call, null);
        } else {
            Call<ResponseBody> call = service.stageBlock(containerName,
                blobName,
                base64BlockId,
                contentLength,
                transactionalContentMD5Converted,
                transactionalContentCrc64Converted,
                body,
                timeout,
                leaseId,
                XMS_VERSION,
                requestId,
                comp,
                encryptionKey,
                encryptionKeySha256,
                encryptionAlgorithm);

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
                    return new CallAndOptionalResult<>(call, result);
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

    private CallAndOptionalResult<BlockBlobsCommitBlockListResponse> commitBlockListIntern(String containerName,
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

        //
        final String comp = "blocklist";
        String transactionalContentMD5Converted = Base64Util.encodeToString(transactionalContentMD5);
        String transactionalContentCrc64Converted = Base64Util.encodeToString(transactionalContentCrc64);
        String contentMd5Converted = Base64Util.encodeToString(contentMd5);
        //
        BlockLookupList blockLookupList = new BlockLookupList().setLatest(base64BlockIds);
        final RequestBody blocks;
        try {
            blocks = RequestBody.create(MediaType.get("application/xml; charset=utf-8"),
                serializerAdapter.serialize(blockLookupList, SerializerFormat.XML));
        } catch (IOException ioe) {
            if (callback != null) {
                callback.onFailure(ioe);
                return null;
            } else {
                throw new RuntimeException(ioe);
            }
        }

        if (callback != null) {
            Call<ResponseBody> call = service.commitBlockList(containerName,
                blobName,
                timeout,
                transactionalContentMD5Converted,
                transactionalContentCrc64Converted,
                metadata,
                leaseId,
                tier,
                ifModifiedSince,
                ifUnmodifiedSince,
                ifMatch,
                ifNoneMatch,
                blocks,
                XMS_VERSION,
                requestId,
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

            executeCall(call, new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        if (response.code() == 201) {
                            BlockBlobCommitBlockListHeaders typedHeader = deserializeHeaders(response.headers(),
                                BlockBlobCommitBlockListHeaders.class);
                            callback.onResponse(new BlockBlobsCommitBlockListResponse(response.raw().request(),
                                response.code(),
                                response.headers(),
                                null,
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
            return new CallAndOptionalResult<>(call, null);
        } else {

            Call<ResponseBody> call = service.commitBlockList(containerName,
                blobName,
                timeout,
                transactionalContentMD5Converted,
                transactionalContentCrc64Converted,
                metadata,
                leaseId,
                tier,
                ifModifiedSince,
                ifUnmodifiedSince,
                ifMatch,
                ifNoneMatch,
                blocks,
                XMS_VERSION,
                requestId,
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

            Response<ResponseBody> response = executeCall(call);

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    BlockBlobCommitBlockListHeaders typedHeader = deserializeHeaders(response.headers(),
                        BlockBlobCommitBlockListHeaders.class);

                    BlockBlobsCommitBlockListResponse result = new BlockBlobsCommitBlockListResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
                        typedHeader);
                    return new CallAndOptionalResult<>(call, result);
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

    private static class CallAndOptionalResult<T> {
        private final Call call;
        private final T result;

        CallAndOptionalResult(Call call, T result) {
            this.call = call;
            this.result = result;
        }

        Call getCall() {
            return this.call;
        }

        T getResult() {
            return this.result;
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
    }
}
