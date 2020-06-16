package com.azure.android.storage.sample.kotlin

import com.azure.android.core.http.Callback
import com.azure.android.storage.blob.StorageBlobClient
import com.azure.android.storage.blob.models.BlobItem
import com.azure.android.storage.blob.models.ContainersListBlobFlatSegmentResponse
import com.azure.android.storage.blob.models.ListBlobsOptions
import com.azure.android.storage.sample.kotlin.core.util.paging.PageItemsFetcher
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescription
import kotlin.collections.ArrayList
import com.azure.android.storage.sample.kotlin.core.util.paging.DefaultPaginationDescription
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationOptions
import com.azure.android.storage.sample.kotlin.core.util.tokenrequest.TokenRequestObservableAuthInterceptor
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescriptionRepository
import com.azure.android.storage.sample.kotlin.core.util.tokenrequest.TokenRequestObservable

/**
 * PACKAGE PRIVATE TYPE.
 */
internal class ContainerBlobsPaginationRepository(storageBlobClient: StorageBlobClient,
                                                  paginationOptions: PaginationOptions) : PaginationDescriptionRepository<BlobItem, String> {
    val storageBlobClient: StorageBlobClient
    private val authInterceptor: TokenRequestObservableAuthInterceptor
    private val paginationOptions: PaginationOptions = paginationOptions

    override val tokenRequestObservable: TokenRequestObservable
        get() = authInterceptor.tokenRequestObservable

    override operator fun get(parameter: String): PaginationDescription<BlobItem> {
        val pageItemsFetcher: PageItemsFetcher<BlobItem> = object : PageItemsFetcher<BlobItem> {
            var options = ListBlobsOptions()
            override fun fetchPage(pageIdentifier: String?, pageSize: Int?, callback: PageItemsFetcher.FetchCallback<BlobItem>) {
                if (pageSize != null && pageSize > 0) {
                    options.maxResultsPerPage = pageSize
                }
                storageBlobClient.getBlobsInPageWithRestResponse(pageIdentifier,
                    parameter, // container-name
                    options.prefix,
                    options.maxResultsPerPage,
                    options.details.toList(),
                    null,
                    null,
                    object : Callback<ContainersListBlobFlatSegmentResponse> {
                        override fun onResponse(response: ContainersListBlobFlatSegmentResponse) {

                            val items : java.util.ArrayList<BlobItem> = if (response.value.segment == null) {
                                ArrayList(0)
                            } else {
                                ArrayList(response.value.segment.blobItems);
                            }
                            callback.onSuccess(items, response.value.marker, response.value.nextMarker)
                        }

                        override fun onFailure(t: Throwable) {
                            callback.onFailure(t)
                        }
                    })
            }
        }
        return DefaultPaginationDescription.create(pageItemsFetcher, paginationOptions)
    }

    init {
        val blobEndpointScopes = java.util.ArrayList<String>()
        blobEndpointScopes.add("$storageBlobClient.blobServiceUrl.default")
        authInterceptor = TokenRequestObservableAuthInterceptor(blobEndpointScopes)
        //
        this.storageBlobClient = storageBlobClient.newBuilder()
            .setCredentialInterceptor(authInterceptor)
            .build()
    }
}
