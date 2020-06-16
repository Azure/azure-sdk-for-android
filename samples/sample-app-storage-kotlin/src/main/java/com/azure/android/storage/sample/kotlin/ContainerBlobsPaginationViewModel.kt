package com.azure.android.storage.sample.kotlin

import com.azure.android.storage.blob.StorageBlobClient
import com.azure.android.storage.blob.models.BlobItem
import java.util.concurrent.Executors
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationViewModel;
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationOptions;

class ContainerBlobsPaginationViewModel(storageBlobClient: StorageBlobClient)
    : PaginationViewModel<PaginationDescriptionRepository<BlobItem, String>, BlobItem, String>(getRepository(storageBlobClient)) {

    companion object {
        private fun getRepository(storageBlobClient: StorageBlobClient): PaginationDescriptionRepository<BlobItem, String> {
            val paginationOptions: PaginationOptions = PaginationOptions(Executors.newFixedThreadPool(3))
                .enableInteractiveLogin(true)
            return ContainerBlobsPaginationRepository(storageBlobClient, paginationOptions)
        }
    }
}
