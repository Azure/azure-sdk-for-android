package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.paging.PageKeyedDataSource

/**
 * PACKAGE PRIVATE.
 *
 * An implementation of [PageKeyedDataSource] that uses [PageItemsFetcher]
 * abstraction to retrieve the pages.
 *
 * @param T the type of items in page
 */
internal class PageItemsFetcherBasedDataSource<T>(pageItemsFetcher: PageItemsFetcher<T>,
                                                  notification: DataSourceNotification) : PageKeyedDataSource<String?, T>() {
    private val pageItemsFetcher: PageItemsFetcher<T> = pageItemsFetcher
    private val notification: DataSourceNotification = notification

    override fun loadInitial(params: LoadInitialParams<String?>,
                             loadCallback: LoadInitialCallback<String?, T>) {
        notification.notifyLoadingState(true)

        val fetchCallback = object : PageItemsFetcher.FetchCallback<T> {
            override fun onSuccess(items: List<T>, currentPageIdentifier: String?, nextPageIdentifier: String?) {
                loadCallback.onResult(items, currentPageIdentifier, nextPageIdentifier)
                notification.notifyLoadedState(true)
            }

            override fun onFailure(throwable: Throwable?) {
                notification.notifyLoadErrorState(true, Runnable { loadInitial(params, loadCallback) })
            }
        }
        pageItemsFetcher.fetchPage(null, params.requestedLoadSize, fetchCallback)
    }

    override fun loadBefore(params: LoadParams<String?>,
                            callback: LoadCallback<String?, T>) {
        // ignored
    }

    override fun loadAfter(params: LoadParams<String?>,
                           loadCallback: LoadCallback<String?, T>) {
        notification.notifyLoadingState(false)
        val fetchCallback = object : PageItemsFetcher.FetchCallback<T> {
            override fun onSuccess(items: List<T>, currentPageIdentifier: String?, nextPageIdentifier: String?) {
                loadCallback.onResult(items, nextPageIdentifier)
                notification.notifyLoadedState(false)
            }

            override fun onFailure(throwable: Throwable?) {
                notification.notifyLoadErrorState(false, Runnable { loadAfter(params, loadCallback) })
            }
        }
        pageItemsFetcher.fetchPage(params.key, params.requestedLoadSize, fetchCallback)
    }

    init {
        this.notification.setInvalidateDataSourceRunnable(Runnable { invalidate() })
    }
}
