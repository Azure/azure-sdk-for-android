package com.anuchandy.learn.msal;

import com.azure.android.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.core.util.paging.PaginationOptions;
import com.azure.android.core.util.paging.PaginationViewModel;
import com.azure.android.storage.blob.StorageBlobClient;
import com.azure.android.storage.blob.models.BlobItem;

import java.util.concurrent.Executors;

public class ContainerBlobsPaginationViewModel
        extends PaginationViewModel<PaginationDescriptionRepository<BlobItem, String>, BlobItem, String> {

    public ContainerBlobsPaginationViewModel(StorageBlobClient storageBlobClient) {
        super(getRepository(storageBlobClient));
    }

    private static PaginationDescriptionRepository<BlobItem, String> getRepository(StorageBlobClient storageBlobClient) {
        PaginationOptions paginationOptions = new PaginationOptions(Executors.newFixedThreadPool(3))
               .enableInteractiveLogin(true);
        return storageBlobClient.getBlobsPaginationRepository(paginationOptions);
    }
}
