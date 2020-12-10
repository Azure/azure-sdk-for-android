package com.azure.android.storage.sample.kotlin

import androidx.work.NetworkType
import com.azure.android.core.credential.TokenRequestObservable
import com.azure.android.core.credential.TokenRequestObservableAuthInterceptor
import com.azure.android.core.http.Callback
import com.azure.android.core.util.CancellationToken
import com.azure.android.storage.blob.StorageBlobAsyncClient
import com.azure.android.storage.blob.models.BlobItem
import com.azure.android.storage.blob.models.BlobsPage
import com.azure.android.storage.blob.models.ListBlobsOptions
import com.azure.android.storage.sample.kotlin.core.util.paging.DefaultPaginationDescription
import com.azure.android.storage.sample.kotlin.core.util.paging.PageItemsFetcher
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescription
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationDescriptionRepository
import com.azure.android.storage.sample.kotlin.core.util.paging.PaginationOptions


import okhttp3.Response

/**
 * PACKAGE PRIVATE TYPE.
 */
internal class ContainerBlobsPaginationRepository(storageBlobAsyncClient: StorageBlobAsyncClient,
                                                  private val paginationOptions: PaginationOptions) : PaginationDescriptionRepository<BlobItem, String> {
    val storageBlobAsyncClient: StorageBlobAsyncClient
    private val authInterceptor: TokenRequestObservableAuthInterceptor

    override val tokenRequestObservable: TokenRequestObservable
        get() = authInterceptor.tokenRequestObservable

    override operator fun get(parameter: String): PaginationDescription<BlobItem> {
        val pageItemsFetcher: PageItemsFetcher<BlobItem> = object : PageItemsFetcher<BlobItem> {
            var options = ListBlobsOptions()
            override fun fetchPage(pageIdentifier: String?, pageSize: Int?, callback: PageItemsFetcher.FetchCallback<BlobItem>) {
                if (pageSize != null && pageSize > 0) {
                    options.maxResultsPerPage = pageSize
                }
                storageBlobAsyncClient.getBlobsInPage(pageIdentifier,
                    parameter, // container-name
                    options.prefix,
                    options.maxResultsPerPage,
                    options.details.toList(),
                    null,
                    CancellationToken.NONE,
                    object : Callback<BlobsPage> {
                        override fun onSuccess(result: BlobsPage, response: Response) {
                            callback.onSuccess(result.items, result.pageId, result.nextPageId)
                        }

                        override fun onFailure(t: Throwable, r: Response) {
                            callback.onFailure(t)
                        }
                    })
            }
        }
        return DefaultPaginationDescription.create(pageItemsFetcher, paginationOptions)
    }

    init {
        val storageBlobUrl = storageBlobAsyncClient.blobServiceUrl
        val blobEndpointScopes = listOf("$storageBlobUrl.default")
        authInterceptor = TokenRequestObservableAuthInterceptor(blobEndpointScopes)
        //
        this.storageBlobAsyncClient = storageBlobAsyncClient.newBuilder("com.azure.android.storage.sample-kotlin.download")
            .setCredentialInterceptor(authInterceptor)
            .setTransferRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
