package com.azure.android.core.util.paging;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.concurrent.Executor;

/**
 * THE TYPE IS FOR INTERNAL USE.
 * (Consumed only by client-sdk but exposed to the user as {@link PaginationDescription} ).
 *
 * A default implementation of {@link PaginationDescription} returned from
 * {@link PaginationDescriptionRepository#get(Object)}.
 *
 * @param <T> the type of items in page
 */
public class DefaultPaginationDescription<T> implements PaginationDescription<T> {
    private final LiveData<PagedList<T>> pagedListObservable;
    private final LiveData<DataSourceNotification> dataSourceNotificationObservable;
    private final LiveData<PageLoadState> loadStateObservable;
    private final LiveData<PageLoadState> refreshStateObservable;
    private final Executor retryExecutor;

    private DefaultPaginationDescription(LiveData<DataSourceNotification> dataSourceNotificationObservable,
                              LiveData<PagedList<T>> pagedListObservable,
                              Executor retryExecutor) {
        this.dataSourceNotificationObservable = dataSourceNotificationObservable;
        this.pagedListObservable = pagedListObservable;
        this.retryExecutor = retryExecutor;
        this.loadStateObservable = DataSourceNotification.getLoadStateObservable(dataSourceNotificationObservable);
        this.refreshStateObservable = DataSourceNotification.getRefreshStateObservable(dataSourceNotificationObservable);
    }

    @Override
    public LiveData<PagedList<T>> getPagedListObservable() {
        return this.pagedListObservable;
    }

    @Override
    public LiveData<PageLoadState> getLoadStateObservable() {
        return this.loadStateObservable;
    }

    @Override
    public LiveData<PageLoadState> getRefreshStateObservable()  {
        return this.refreshStateObservable;
    }

    @Override
    public void retry() {
        DataSourceNotification notification = this.dataSourceNotificationObservable.getValue();
        if (notification != null) {
            Runnable retryRunnable = notification.getRetryRunnable();
            if (retryRunnable != null) {
                this.retryExecutor.execute(retryRunnable);
            }
        }
    }

    @Override
    public void refresh() {
        DataSourceNotification notification = this.dataSourceNotificationObservable.getValue();
        if (notification != null) {
            notification.invalidateDataSource();
        }
    }

    public static <T> PaginationDescription<T> create(PageItemsFetcher<T> pageItemsFetcher, PaginationOptions options) {
        PagedList.Config config = options.getPagedListConfig();
        if (config == null) {
            config = new PagedList.Config.Builder().build();
        }

        PageItemsFetcherBasedDataSourceFactory dataSourceFactory = new PageItemsFetcherBasedDataSourceFactory(pageItemsFetcher);

        LiveData<PagedList<T>> pagedListObservable = new LivePagedListBuilder(dataSourceFactory, config)
                    .setFetchExecutor(options.getPageLoadExecutor())
                    .build();

        LiveData<DataSourceNotification> dataSourceNotificationObservable = dataSourceFactory.getDataSourceNotificationObservable();
        return new DefaultPaginationDescription<>(dataSourceNotificationObservable,
                pagedListObservable,
                options.getPageLoadExecutor());
    }
}
