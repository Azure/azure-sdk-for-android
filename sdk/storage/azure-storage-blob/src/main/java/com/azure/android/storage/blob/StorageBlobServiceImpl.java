// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.storage.blob;

import com.azure.android.core.http.Callback;
import com.azure.android.core.http.ServiceClient;
import com.azure.android.core.internal.util.serializer.SerializerAdapter;
import com.azure.android.core.internal.util.serializer.SerializerFormat;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobStorageException;
import com.azure.android.storage.blob.models.ContainerListBlobFlatSegmentHeaders;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
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
    }
}
