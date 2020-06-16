package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import java.util.concurrent.Executor

/**
 * THE TYPE IS FOR INTERNAL USE.
 * (Consumed only by client-sdk but exposed to the user as [PaginationDescription] ).
 *
 * A default implementation of [PaginationDescription] returned from
 * [PaginationDescriptionRepository.get].
 *
 * @param T the type of items in page
 */
class DefaultPaginationDescription<T> private constructor(mDataSourceNotificationObservable: LiveData<DataSourceNotification>,
                                                          mPagedListObservable: LiveData<PagedList<T>>,
                                                          mRetryExecutor: Executor) : PaginationDescription<T> {

    private val mDataSourceNotificationObservable: LiveData<DataSourceNotification> = mDataSourceNotificationObservable
    private val mPagedListObservable: LiveData<PagedList<T>> = mPagedListObservable
    private val mRetryExecutor: Executor = mRetryExecutor

    private val mLoadStateObservable: LiveData<PageLoadState>
        = DataSourceNotification.getLoadStateObservable(mDataSourceNotificationObservable)
    private val mRefreshStateObservable: LiveData<PageLoadState>
        = DataSourceNotification.getRefreshStateObservable(mDataSourceNotificationObservable)

    override val pagedListObservable: LiveData<PagedList<T>>
        get() = mPagedListObservable

    override val loadStateObservable: LiveData<PageLoadState>
        get() = mLoadStateObservable

    override val refreshStateObservable: LiveData<PageLoadState>
        get() = mRefreshStateObservable

    override fun retry() {
        val notification: DataSourceNotification? = mDataSourceNotificationObservable.value
        if (notification != null) {
            val retryRunnable: Runnable? = notification.getRetryRunnable()
            if (retryRunnable != null) {
                mRetryExecutor.execute(retryRunnable)
            }
        }
    }

    override fun refresh() {
        val notification: DataSourceNotification? = mDataSourceNotificationObservable.value
        notification?.invalidateDataSource()
    }

    companion object {
        fun <T> create(pageItemsFetcher: PageItemsFetcher<T>, options: PaginationOptions): PaginationDescription<T> {
            var config: PagedList.Config? = options.pagedListConfig
            if (config == null) {
                config = PagedList.Config.Builder().build()
            }
            val dataSourceFactory = PageItemsFetcherBasedDataSourceFactory(pageItemsFetcher)
            val pagedListObservable: LiveData<PagedList<T>>
                = LivePagedListBuilder<String?, T>(dataSourceFactory, config)
                    .setFetchExecutor(options.pageLoadExecutor)
                    .build()
            val dataSourceNotificationObservable: LiveData<DataSourceNotification>
                = dataSourceFactory.getDataSourceNotificationObservable()
            return DefaultPaginationDescription(dataSourceNotificationObservable,
                pagedListObservable,
                options.pageLoadExecutor)
        }
    }
}
