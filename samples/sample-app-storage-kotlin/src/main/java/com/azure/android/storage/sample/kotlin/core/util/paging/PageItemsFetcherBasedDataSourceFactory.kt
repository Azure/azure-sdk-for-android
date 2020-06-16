package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

/**
 * PACKAGE PRIVATE.
 *
 * An implementation of [DataSource] that creates [PageItemsFetcherBasedDataSource].
 *
 * @param T the type of items in page
 */
internal class PageItemsFetcherBasedDataSourceFactory<T>(pageItemsFetcher: PageItemsFetcher<T>) : DataSource.Factory<String?, T?>() {
    private val dataSourceNotificationObservable: MutableLiveData<DataSourceNotification> = MutableLiveData<DataSourceNotification>()
    private val pageItemsFetcher: PageItemsFetcher<T> = pageItemsFetcher

    /**
     * @return the LiveData based Observable that when a [DataSource] gets created will emits
     * [DataSourceNotification] object assigned to the created data source.
     */
    fun getDataSourceNotificationObservable(): LiveData<DataSourceNotification> {
        return dataSourceNotificationObservable
    }

    override fun create(): DataSource<String?, T?> {
        val notification: DataSourceNotification = DataSourceNotification()
        val dataSource = PageItemsFetcherBasedDataSource(pageItemsFetcher, notification)
        dataSourceNotificationObservable.postValue(notification)
        return dataSource
    }
}
