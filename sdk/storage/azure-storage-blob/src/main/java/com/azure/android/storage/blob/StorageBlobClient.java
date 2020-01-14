package com.azure.android.storage.blob;

import com.azure.android.core.http.interceptors.AddDateInterceptor;
import com.azure.android.core.http.rest.RestAPIClient;
import com.azure.android.core.http.rest.RestCallBack;
import com.azure.android.core.implementation.util.serializer.SerializerEncoding;
import com.azure.android.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.core.util.paging.PaginationOptions;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsIncludeItem;
import com.azure.android.storage.blob.models.ListBlobsOptions;

import java.util.List;
import java.util.Objects;

import okhttp3.Interceptor;

public class StorageBlobClient {
    private final RestAPIClient restAPIClient;
    private final StorageBlobServiceImpl serviceClient;

    private StorageBlobClient(RestAPIClient restAPIClient) {
        this.restAPIClient = restAPIClient;
        this.serviceClient = new StorageBlobServiceImpl(this.restAPIClient);
    }

    public StorageBlobClient.Builder newBuilder() {
        return new Builder(this);
    }

    public String getBlobUrl() {
        return this.restAPIClient.getBaseUrl();
    }

    public PaginationDescriptionRepository<BlobItem, String> getBlobsPaginationRepository(PaginationOptions options) {
        return new ContainerBlobsPaginationRepository(this, options);
    }

    // TODO: [Jonathan feedback] use IterableStream
    public List<BlobItem> getBlobsInPage(String pageId,
                                         String containerName,
                                         ListBlobsOptions options) {
        return this.serviceClient.getBlobsInPage(pageId, containerName, options);
    }

    public void getBlobsInPage(String pageId,
                               String containerName,
                               ListBlobsOptions options,
                               RestCallBack<List<BlobItem>> callback) {
        this.serviceClient.getBlobsInPage(pageId, containerName, options, callback);
    }

    public ContainersListBlobFlatSegmentResponse getBlobsInPageWithRestResponse(String pageId,
                                                                                String containerName,
                                                                                String prefix,
                                                                                Integer maxResults,
                                                                                List<ListBlobsIncludeItem> include,
                                                                                Integer timeout,
                                                                                String requestId) {
        return this.serviceClient.getBlobsInPageWithRestResponse(pageId, containerName,
                prefix,
                maxResults,
                include,
                timeout,
                requestId);
    }

    public void getBlobsInPageWithRestResponse(String pageId,
                                               String containerName,
                                               String prefix,
                                               Integer maxResults,
                                               List<ListBlobsIncludeItem> include,
                                               Integer timeout,
                                               String requestId,
                                               RestCallBack<ContainersListBlobFlatSegmentResponse> callback) {
        this.serviceClient.getBlobsInPageWithRestResponse(pageId,
                containerName,
                prefix,
                maxResults,
                include,
                timeout,
                requestId,
                callback);
    }

    public static class Builder {
        private final RestAPIClient.Builder restAPIClientBuilder;

        public Builder() {
            this(new RestAPIClient.Builder());
            this.restAPIClientBuilder
                    .addInterceptor(new AddDateInterceptor())
                    .setSerializationFormat(SerializerEncoding.XML);
        }

        public Builder(RestAPIClient.Builder restAPIClientBuilder) {
            Objects.requireNonNull(restAPIClientBuilder, "restAPIClientBuilder cannot be null.");
            this.restAPIClientBuilder = restAPIClientBuilder;
        }

        public Builder setBlobUrl(String blobUrl) {
            Objects.requireNonNull(blobUrl, "blobUrl cannot be null.");
            this.restAPIClientBuilder.setBaseUrl(blobUrl);
            return this;
        }

        public Builder setCredentialInterceptor(Interceptor credentialInterceptor) {
            this.restAPIClientBuilder.setCredentialsInterceptor(credentialInterceptor);
            return this;
        }

        public StorageBlobClient build() {
            return new StorageBlobClient(this.restAPIClientBuilder.build());
        }

        private Builder(final StorageBlobClient storageBlobClient) {
            this(storageBlobClient.restAPIClient.newBuilder());
        }
    }
}
