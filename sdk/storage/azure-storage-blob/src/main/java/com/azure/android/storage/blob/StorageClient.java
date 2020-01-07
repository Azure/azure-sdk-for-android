package com.azure.android.storage.blob;

import com.azure.android.core.http.rest.RestCallBack;
import com.azure.android.core.http.rest.RetrofitAPIClient;
import com.azure.android.core.implementation.util.serializer.JacksonAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerAdapter;
import com.azure.android.core.implementation.util.serializer.SerializerEncoding;
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
import okhttp3.Interceptor;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class StorageClient {
    private final String serviceUrl;
    private final SerializerAdapter serializerAdapter;
    private final StorageService service;
    private static String xmsVersion = "2019-02-02";

    public StorageClient(String serviceUrl, List<Interceptor> interceptors) {
        this.serviceUrl = serviceUrl;
        this.serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        this.service = RetrofitAPIClient.createAPIClient(this.serviceUrl,
                SerializerEncoding.XML,
                this.serializerAdapter,
                interceptors,
                StorageService.class);
    }

    public List<BlobItem> getBlobsInPage(String pageId,
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

    public void getBlobsInPage(String pageId,
                               String containerName,
                               ListBlobsOptions options,
                               RestCallBack<List<BlobItem>> callback) {
        options = options == null ? new ListBlobsOptions() : options;
        this.getBlobsInPageWithRestResponse(pageId, containerName, options.getPrefix(),
                options.getMaxResultsPerPage(), options.getDetails().toList(),
                null, null, new RestCallBack<ContainersListBlobFlatSegmentResponse>() {
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

    public ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponse(String pageId,
                                                                                String containerName,
                                                                                String prefix,
                                                                                Integer maxResults,
                                                                                List<ListBlobsIncludeItem> include,
                                                                                Integer timeout,
                                                                                String requestId
                                                                                /*Context context*/) {
        return getBlobsInPageWithRestResponseIntern(pageId, containerName,
                prefix,
                maxResults,
                include,
                timeout,
                requestId,
                null);
    }

    public void getBlobsInPageWithRestResponse(String pageId,
                                               String containerName,
                                               String prefix,
                                               Integer maxResults,
                                               List<ListBlobsIncludeItem> include,
                                               Integer timeout,
                                               String requestId,
                                               /*Context context*/
                                               RestCallBack<ContainersListBlobFlatSegmentResponse> callback) {
        getBlobsInPageWithRestResponseIntern(pageId,
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
                                                                                        /*Context context*/
                                                                                       RestCallBack<ContainersListBlobFlatSegmentResponse> callback) {
        final String resType = "container";
        final String comp = "list";
        if (callback != null) {
            executeCall(service.listBlobFlatSegment(containerName,
                    prefix,
                    pageId,
                    maxResults,
                    serializerAdapter.serializeList(include, SerializerAdapter.CollectionFormat.CSV),
                    timeout,
                    xmsVersion, // TODO: this.client.getVersion(),
                    requestId,
                    resType,
                    comp
                    /*context*/), new Callback<ResponseBody>() {
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
                    serializerAdapter.serializeList(include, SerializerAdapter.CollectionFormat.CSV),
                    timeout,
                    xmsVersion, // TODO: this.client.getVersion(),
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


    private static <T> retrofit2.Response<T> executeCall(retrofit2.Call<T> call) {
        try {
            return call.execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> void executeCall(retrofit2.Call<T> call, retrofit2.Callback<T> callback) {
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
            return this.serializerAdapter.deserialize(str, type, SerializerEncoding.XML);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private <T> T deserializeHeaders(Headers headers,  Type type) {
        try {
            return this.serializerAdapter.deserialize(headers, type);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private interface StorageService {
        @GET("{containerName}")
        Call<ResponseBody> listBlobFlatSegment(@Path("containerName") String containerName,
                                                         // @HostParam("url") String url,
                                                         @Query("prefix") String prefix,
                                                         @Query("marker") String marker,
                                                         @Query("maxresults") Integer maxResults,
                                                         @Query("include") String include,
                                                         @Query("timeout") Integer timeout,
                                                         @Header("x-ms-version") String version,
                                                         @Header("x-ms-client-request-id") String requestId,
                                                         @Query("restype") String resType,
                                                         @Query("comp") String comp
                /*, Context context*/);

    }
}
