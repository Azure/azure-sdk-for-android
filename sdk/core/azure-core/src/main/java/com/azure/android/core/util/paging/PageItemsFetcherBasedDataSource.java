package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import java.util.List;

/**
 * PACKAGE PRIVATE.
 *
 * An implementation of {@link PageKeyedDataSource} that uses {@link PageItemsFetcher}
 * abstraction to retrieve the pages.
 *
 * @param <T> the type of items in page
 */
class PageItemsFetcherBasedDataSource<T> extends PageKeyedDataSource<String, T> {
    private final PageItemsFetcher<T> pageItemsFetcher;
    private final DataSourceNotification notification;

    /**
     *  PACKAGE PRIVATE CTR.
     *
     *  Creates PageItemsFetcherBasedDataSource.
     *
     * @param pageItemsFetcher the fetcher to request pages
     * @param notification an object to notify various events in the DataSource
     */
    PageItemsFetcherBasedDataSource(PageItemsFetcher<T> pageItemsFetcher,
                                    DataSourceNotification notification) {
        this.pageItemsFetcher = pageItemsFetcher;
        this.notification = notification;
        this.notification.setInvalidateDataSourceRunnable(() -> {
            invalidate();
        });
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params,
                            @NonNull LoadInitialCallback<String, T> loadCallback) {
        this.notification.notifyLoadingState(true);
        this.pageItemsFetcher.fetchPage(null, params.requestedLoadSize, new PageItemsFetcher.FetchCallback<T>() {
            @Override
            public void onSuccess(List<T> items, String currentPageIdentifier, String nextPageIdentifier) {
                loadCallback.onResult(items, currentPageIdentifier, nextPageIdentifier);
                notification.notifyLoadedState(true);
            }

            @Override
            public void onFailure(Throwable throwable) {
                notification.notifyLoadErrorState(true, () -> loadInitial(params, loadCallback));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params,
                           @NonNull LoadCallback<String, T> callback) {
        // ignored
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params,
                          @NonNull LoadCallback<String, T> loadCallback) {
        this.notification.notifyLoadingState(false);
        this.pageItemsFetcher.fetchPage(params.key, params.requestedLoadSize, new PageItemsFetcher.FetchCallback<T>() {
            @Override
            public void onSuccess(List<T> items, String currentPageIdentifier, String nextPageIdentifier) {
                loadCallback.onResult(items, nextPageIdentifier);
                notification.notifyLoadedState(false);
            }

            @Override
            public void onFailure(Throwable throwable) {
                notification.notifyLoadErrorState(false, () -> loadAfter(params, loadCallback));
            }
        });
    }
}
