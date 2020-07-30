package com.azure.android.storage.sample;

import com.azure.android.storage.blob.StorageBlobAsyncClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;
import com.azure.android.storage.sample.core.util.paging.PaginationViewModel;

import java.util.concurrent.Executors;

public class ContainerBlobsPaginationViewModel
        extends PaginationViewModel<PaginationDescriptionRepository<BlobItem, String>, BlobItem, String> {

    public ContainerBlobsPaginationViewModel(StorageBlobAsyncClient storageBlobAsyncClient) {
        super(getRepository(storageBlobAsyncClient));
    }

    private static PaginationDescriptionRepository<BlobItem, String> getRepository(StorageBlobAsyncClient storageBlobAsyncClient) {
        PaginationOptions paginationOptions = new PaginationOptions(Executors.newFixedThreadPool(3))
               .enableInteractiveLogin(true);
        return new ContainerBlobsPaginationRepository(storageBlobAsyncClient, paginationOptions);
    }
}
