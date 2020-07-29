package com.azure.android.storage.sample;

import androidx.work.NetworkType;

import com.azure.android.core.credential.TokenRequestObservable;
import com.azure.android.core.credential.TokenRequestObservableAuthInterceptor;
import com.azure.android.core.http.Callback;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.sample.core.util.paging.DefaultPaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PageItemsFetcher;
import com.azure.android.storage.sample.core.util.paging.PaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PACKAGE PRIVATE TYPE.
 */
final class ContainerBlobsPaginationRepository
        implements PaginationDescriptionRepository<BlobItem, String> {

    private final StorageBlobAsyncClient storageBlobAsyncClient;
    private final TokenRequestObservableAuthInterceptor authInterceptor;
    private final PaginationOptions paginationOptions;

    ContainerBlobsPaginationRepository(StorageBlobAsyncClient storageBlobAsyncClient, PaginationOptions paginationOptions) {
        this.paginationOptions = paginationOptions;
        final String storageBlobUrl = storageBlobAsyncClient.getBlobServiceUrl();
        final List<String> blobEndpointScopes = Arrays.asList(storageBlobUrl + ".default");
        this.authInterceptor = new TokenRequestObservableAuthInterceptor(blobEndpointScopes);
        //
        this.storageBlobAsyncClient = storageBlobAsyncClient.newBuilder("com.azure.android.storage.sample.download")
                .setCredentialInterceptor(this.authInterceptor)
                .setTransferRequiredNetworkType(NetworkType.CONNECTED)
                .build();
    }

    public StorageBlobAsyncClient getStorageBlobClient() {
        return storageBlobAsyncClient;
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
                storageBlobAsyncClient.getBlobsInPageWithRestResponse(pageIdentifier, containerName, options.getPrefix(),
                        options.getMaxResultsPerPage(), options.getDetails().toList(),
                        null, null, CancellationToken.NONE, new Callback<ContainersListBlobFlatSegmentResponse>() {
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
