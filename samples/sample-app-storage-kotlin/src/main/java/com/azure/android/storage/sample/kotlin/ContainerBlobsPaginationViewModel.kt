package com.azure.android.storage.sample.kotlin

import com.azure.android.storage.blob.StorageBlobAsyncClient
import com.azure.android.storage.blob.models.BlobItem
import java.util.concurrent.Executors
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationViewModel;
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescriptionRepository;
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationOptions;

class ContainerBlobsPaginationViewModel(storageBlobAsyncClient: StorageBlobAsyncClient)
    : PaginationViewModel<PaginationDescriptionRepository<BlobItem, String>, BlobItem, String>(getRepository(storageBlobAsyncClient)) {

    companion object {
        private fun getRepository(storageBlobAsyncClient: StorageBlobAsyncClient): PaginationDescriptionRepository<BlobItem, String> {
            val paginationOptions: PaginationOptions = PaginationOptions(Executors.newFixedThreadPool(3))
                .enableInteractiveLogin(true)
            return ContainerBlobsPaginationRepository(storageBlobAsyncClient, paginationOptions)
        }
    }
}
