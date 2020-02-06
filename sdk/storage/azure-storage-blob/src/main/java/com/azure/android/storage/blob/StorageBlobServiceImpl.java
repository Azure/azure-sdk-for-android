// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.core.util.Base64Util;
import com.azure.android.core.util.DateTimeRfc1123;
import com.azure.android.storage.blob.models.AccessTier;
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

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    byte[] blockContent,
                    byte[] contentMd5) {
        this.stageBlockWithRestResponse(containerName,
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

    void stageBlock(String containerName,
                    String blobName,
                    String base64BlockId,
                    byte[] blockContent,
                    byte[] contentMd5,
                    Callback<Void> callback) {
        this.stageBlockWithRestResponse(containerName,
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
            null);
    }

    void stageBlockWithRestResponse(String containerName,
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
        this.stageBlockWithRestResponseIntern(containerName,
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
    }

    BlockBlobItem commitBlockList(String containerName,
                                  String blobName,
                                  List<String> base64BlockIds,
                                  boolean overwrite) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch( "*");
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

    void commitBlockList(String containerName,
                         String blobName,
                         List<String> base64BlockIds,
                         boolean overwrite,
                         Callback<BlockBlobItem> callBack) {
        BlobRequestConditions requestConditions = null;
        if (!overwrite) {
            requestConditions = new BlobRequestConditions().setIfNoneMatch( "*");
        }
        this.commitBlockListWithRestResponse(containerName,
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
            null);
    }

    void commitBlockListWithRestResponse(String containerName,
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
        this.commitBlockListIntern(containerName,
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

    private BlockBlobsStageBlockResponse stageBlockWithRestResponseIntern(String containerName,
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
            executeCall(service.stageBlock(containerName,
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
                encryptionAlgorithm), new retrofit2.Callback<ResponseBody>() {
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
            return null;
        } else {
            Response<ResponseBody> response = executeCall(service.stageBlock(containerName,
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
                encryptionAlgorithm));

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    BlockBlobStageBlockHeaders typedHeader = deserializeHeaders(response.headers(),
                        BlockBlobStageBlockHeaders.class);

                    return new BlockBlobsStageBlockResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
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


    private BlockBlobsCommitBlockListResponse commitBlockListIntern(String containerName,
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
            executeCall(service.commitBlockList(containerName,
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
                encryptionAlgorithm), new retrofit2.Callback<ResponseBody>() {
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
            return null;
        } else {
            Response<ResponseBody> response = executeCall(service.commitBlockList(containerName,
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
                encryptionAlgorithm));

            if (response.isSuccessful()) {
                if (response.code() == 201) {
                    BlockBlobCommitBlockListHeaders typedHeader = deserializeHeaders(response.headers(),
                        BlockBlobCommitBlockListHeaders.class);

                    return new BlockBlobsCommitBlockListResponse(response.raw().request(),
                        response.code(),
                        response.headers(),
                        null,
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
                                           @Header("x-ms-meta-") Map<String, String> metadata, // TODO:anuchan
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
