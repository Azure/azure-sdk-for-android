package com.azure.android.storage.sample;

import androidx.work.NetworkType;

import com.azure.android.core.http.Callback;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.sample.core.util.paging.DefaultPaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PageItemsFetcher;
import com.azure.android.storage.sample.core.util.paging.PaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservable;
import com.azure.android.storage.sample.core.util.tokenrequest.TokenRequestObservableAuthInterceptor;

import java.util.Arrays;
import java.util.List;

import okhttp3.Response;

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
                options.setCancellationToken(CancellationToken.NONE);

                storageBlobAsyncClient.getBlobsInPage(pageIdentifier, containerName, options, new Callback<BlobsPage>() {
                        @Override
                        public void onSuccess(BlobsPage value, Response response) {
                            callback.onSuccess(value.getItems(), value.getPageId(), value.getNextPageId());
                        }

                        @Override
                        public void onFailure(Throwable t, Response response) {

                        }
                    });
            }
        };
        return DefaultPaginationDescription.create(pageItemsFetcher, this.paginationOptions);
    }
}
