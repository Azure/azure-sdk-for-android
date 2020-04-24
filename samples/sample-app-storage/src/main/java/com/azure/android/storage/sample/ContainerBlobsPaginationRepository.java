package com.azure.android.storage.sample;

import com.azure.android.core.http.Callback;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.sample.core.util.paging.DefaultPaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PageItemsFetcher;
import com.azure.android.storage.sample.core.util.paging.PaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservable;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservableAuthInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PACKAGE PRIVATE TYPE.
 */
final class ContainerBlobsPaginationRepository
        implements PaginationDescriptionRepository<BlobItem, String> {

    private final StorageBlobClient storageBlobClient;
    private final TokenRequestObservableAuthInterceptor authInterceptor;
    private final PaginationOptions paginationOptions;

    ContainerBlobsPaginationRepository(StorageBlobClient storageBlobClient, PaginationOptions paginationOptions) {
        this.paginationOptions = paginationOptions;
        final String storageBlobUrl = storageBlobClient.getBlobServiceUrl();
        final List<String> blobEndpointScopes = Arrays.asList(storageBlobUrl + ".default");
        this.authInterceptor = new TokenRequestObservableAuthInterceptor(blobEndpointScopes);
        //
        this.storageBlobClient = storageBlobClient.newBuilder()
                .setCredentialInterceptor(this.authInterceptor)
                .build();
    }

    public StorageBlobClient getStorageBlobClient() {
        return storageBlobClient;
    }

    @Override
    public TokenRequestObservable getTokenRequestObservable() {
        return this.authInterceptor.getTokenRequestObservable();
    }

    @Override
    public PaginationDescription<BlobItem> get(String containerName) {
        PageItemsFetcher<BlobItem> pageItemsFetcher = new PageItemsFetcher<BlobItem>() {
            ListBlobsOptions options = new ListBlobsOptions();
            @Override
            public void fetchPage(String pageIdentifier, Integer pageSize, FetchCallback<BlobItem> callback) {
                if (pageSize != null && pageSize > 0) {
                    options.setMaxResultsPerPage(pageSize);
                }
                storageBlobClient.getBlobsInPageWithRestResponse(pageIdentifier, containerName, options.getPrefix(),
                        options.getMaxResultsPerPage(), options.getDetails().toList(),
                        null, null, new Callback<ContainersListBlobFlatSegmentResponse>() {
                            @Override
                            public void onResponse(ContainersListBlobFlatSegmentResponse response) {
                                List<BlobItem> value = response.getValue().getSegment() == null
                                        ? new ArrayList<>(0)
                                       : response.getValue().getSegment().getBlobItems();
                               callback.onSuccess(value, response.getValue().getMarker(), response.getValue().getNextMarker());
                           }

                         @Override
                            public void onFailure(Throwable t) {
                            callback.onFailure(t);
                        }
                 });
            }
        };
        return DefaultPaginationDescription.create(pageItemsFetcher, this.paginationOptions);
    }
}
