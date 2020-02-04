package com.azure.android.storage.sample.core.util.paging;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Objects;

/**
 * PACKAGE PRIVATE TYPE.
 *
 * A type that {@link PageItemsFetcherBasedDataSource} uses to notify its current state
 * when {@link androidx.paging.PagedList} request the DataSource to load pages.
 *
 */
class DataSourceNotification {
    private final MutableLiveData<PageLoadState> loadStateObservable = new MutableLiveData<>();
    private final MutableLiveData<PageLoadState> refreshStateObservable = new MutableLiveData<>();
    private volatile Runnable retryRunnable;
    private Runnable invalidateDataSourceRunnable;

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Notify that DataSource is loading a page.
     *
     * @param isInitial indicate is it first page load or not
     */
    void notifyLoadingState(boolean isInitial) {
        this.loadStateObservable.postValue(PageLoadState.LOADING);
        if (isInitial) {
            this.refreshStateObservable.postValue(PageLoadState.LOADING);
        }
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Notify that DataSource is done with loading a page.
     *
     * @param isInitial indicate is it first page load or not
     */
    void notifyLoadedState(boolean isInitial) {
        this.loadStateObservable.postValue(PageLoadState.LOADED);
        if (isInitial) {
            this.refreshStateObservable.postValue(PageLoadState.LOADED);
        }
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Notify that DataSource's attempt to load the page is failed
     *
     * @param isInitial indicate is it first page load or not
     * @param retryRunnable a runnable that can be called to re-attempt the failed load
     */
    void notifyLoadErrorState(boolean isInitial, Runnable retryRunnable) {
        this.retryRunnable = retryRunnable;
        this.loadStateObservable.postValue(PageLoadState.FAILED);
        if (isInitial) {
            this.refreshStateObservable.postValue(PageLoadState.FAILED);
        }
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * @return the LiveData observable that emits page load status
     */
    static LiveData<PageLoadState> getLoadStateObservable(LiveData<DataSourceNotification> notificationObservable) {
        return Transformations.switchMap(notificationObservable, n -> n.loadStateObservable);
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     *@return the LiveData observable that emits refresh state
     */
    static LiveData<PageLoadState> getRefreshStateObservable(LiveData<DataSourceNotification> notificationObservable) {
        return Transformations.switchMap(notificationObservable, n -> n.refreshStateObservable);
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * A runnable that upon invocation retries the last failed page load.
     */
     Runnable getRetryRunnable() {
        Runnable runnable = this.retryRunnable;
        this.retryRunnable = null;
        return runnable;
     }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Sets the runnable which on invocation invalidate the DataSource, any LiveDataPagedList
     * based on the current DataSource will listen for such invalidation and re-create the
     * DataSource then creates a PagedList emit to Observers.
     *
     * @param runnable the refresh runnable
     */
    void setInvalidateDataSourceRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable);
        if (this.invalidateDataSourceRunnable != null) {
            throw new IllegalStateException("Invalidate runnable is already set.");
        }
        this.invalidateDataSourceRunnable = runnable;
    }

    /**
     * PACKAGE PRIVATE METHOD.
     *
     * Invalidate the DataSource associated with this notification.
     */
    void invalidateDataSource() {
        Objects.requireNonNull(this.invalidateDataSourceRunnable);
        this.invalidateDataSourceRunnable.run();
    }
}
