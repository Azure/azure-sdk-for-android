package com.azure.android.storage.sample.core.util.paging;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

/**
 * PACKAGE PRIVATE.
 *
 * An implementation of {@link DataSource} that creates {@link PageItemsFetcherBasedDataSource}.
 *
 * @param <T> the type of items in page
 */
class PageItemsFetcherBasedDataSourceFactory<T> extends DataSource.Factory<String, T> {
    private final MutableLiveData<DataSourceNotification> dataSourceNotificationObservable = new MutableLiveData<>();
    private final PageItemsFetcher<T> pageItemsFetcher;

    /**
     * PACKAGE PRIVATE CTR.
     *
     * Creates PageItemsFetcherBasedDataSourceFactory.
     *
     * @param pageItemsFetcher the page fetcher for the {@link PageItemsFetcherBasedDataSource} data source
     *                         this factory creates
     */
    PageItemsFetcherBasedDataSourceFactory(PageItemsFetcher<T> pageItemsFetcher) {
        this.pageItemsFetcher = pageItemsFetcher;
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * @return the LiveData based Observable that when a {@link DataSource} gets created will emits
     * {@link DataSourceNotification} object assigned to the created data source.
     */
    LiveData<DataSourceNotification> getDataSourceNotificationObservable() {
        return this.dataSourceNotificationObservable;
    }

    @NonNull
    @Override
    public DataSource<String, T> create() {
        DataSourceNotification notification = new DataSourceNotification();
        PageItemsFetcherBasedDataSource dataSource = new PageItemsFetcherBasedDataSource(this.pageItemsFetcher, notification);
        this.dataSourceNotificationObservable.postValue(notification);
        return dataSource;
    }
}
