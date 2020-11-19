package com.azure.android.storage.sample;

import androidx.work.NetworkType;

import com.azure.android.core.credential.TokenRequestObservable;
import com.azure.android.core.credential.TokenRequestObservableAuthInterceptor;
import com.azure.android.core.http.Callback;
import com.azure.android.core.util.CancellationToken;
import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.blob.models.BlobsPage;
import com.azure.android.storage.blob.models.ListBlobsOptions;
import com.azure.android.storage.blob.options.ContainerListBlobsOptions;
import com.azure.android.storage.sample.core.util.paging.DefaultPaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PageItemsFetcher;
import com.azure.android.storage.sample.core.util.paging.PaginationDescription;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;

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
                storageBlobAsyncClient.getBlobsInPage(new ContainerListBlobsOptions(containerName).setPageId(pageIdentifier)
                    .setListBlobsOptions(options).setCancellationToken(CancellationToken.NONE),
                    new Callback<BlobsPage>() {
                        @Override
                        public void onSuccess(BlobsPage result, Response response) {
                            List<BlobItem> value = result.getItems();
                            callback.onSuccess(value, result.getPageId(), result.getNextPageId());
                        }

                        @Override
                        public void onFailure(Throwable throwable, Response response) {
                            callback.onFailure(throwable);
                        }
                    });
            }
        };
        return DefaultPaginationDescription.create(pageItemsFetcher, this.paginationOptions);
    }
}
