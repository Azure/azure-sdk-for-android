package com.azure.android.storage.sample;

import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;
import com.azure.android.storage.sample.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.core.util.paging.PaginationOptions;
import com.azure.android.storage.sample.core.util.paging.PaginationViewModel;

import java.util.concurrent.Executors;

public class ContainerBlobsPaginationViewModel
        extends PaginationViewModel<PaginationDescriptionRepository<BlobItem, String>, BlobItem, String> {

    public ContainerBlobsPaginationViewModel(StorageBlobClient storageBlobClient) {
        super(getRepository(storageBlobClient));
    }

    private static PaginationDescriptionRepository<BlobItem, String> getRepository(StorageBlobClient storageBlobClient) {
        PaginationOptions paginationOptions = new PaginationOptions(Executors.newFixedThreadPool(3))
               .enableInteractiveLogin(true);
        return new ContainerBlobsPaginationRepository(storageBlobClient, paginationOptions);
    }
}
