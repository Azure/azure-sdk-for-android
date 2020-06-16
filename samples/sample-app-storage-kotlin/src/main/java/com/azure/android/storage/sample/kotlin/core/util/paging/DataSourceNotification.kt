package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.util.*

/**
 * PACKAGE PRIVATE TYPE.
 *
 * A type that [PageItemsFetcherBasedDataSource] uses to notify its current state
 * when [androidx.paging.PagedList] request the DataSource to load pages.
 *
 */
internal class DataSourceNotification {
    private val loadStateObservable: MutableLiveData<PageLoadState> = MutableLiveData<PageLoadState>()
    private val refreshStateObservable: MutableLiveData<PageLoadState> = MutableLiveData<PageLoadState>()

    @Volatile
    private var retryRunnable: Runnable? = null
    private var invalidateDataSourceRunnable: Runnable? = null

    /**
     * Notify that DataSource is loading a page.
     *
     * @param isInitial indicate is it first page load or not
     */
    fun notifyLoadingState(isInitial: Boolean) {
        loadStateObservable.postValue(PageLoadState.LOADING)
        if (isInitial) {
            refreshStateObservable.postValue(PageLoadState.LOADING)
        }
    }

    /**
     * Notify that DataSource is done with loading a page.
     *
     * @param isInitial indicate is it first page load or not
     */
    fun notifyLoadedState(isInitial: Boolean) {
        loadStateObservable.postValue(PageLoadState.LOADED)
        if (isInitial) {
            refreshStateObservable.postValue(PageLoadState.LOADED)
        }
    }

    /**
     * Notify that DataSource's attempt to load the page is failed
     *
     * @param isInitial indicate is it first page load or not
     * @param retryRunnable a runnable that can be called to re-attempt the failed load
     */
    fun notifyLoadErrorState(isInitial: Boolean, retryRunnable: Runnable?) {
        this.retryRunnable = retryRunnable
        loadStateObservable.postValue(PageLoadState.FAILED)
        if (isInitial) {
            refreshStateObservable.postValue(PageLoadState.FAILED)
        }
    }

    /**
     * A runnable that upon invocation retries the last failed page load.
     */
    fun getRetryRunnable(): Runnable? {
        val runnable = retryRunnable
        retryRunnable = null
        return runnable
    }

    /**
     * Sets the runnable which on invocation invalidate the DataSource, any LiveDataPagedList
     * based on the current DataSource will listen for such invalidation and re-create the
     * DataSource then creates a PagedList emit to Observers.
     *
     * @param runnable the refresh runnable
     */
    fun setInvalidateDataSourceRunnable(runnable: Runnable) {
        Objects.requireNonNull(runnable)
        check(invalidateDataSourceRunnable == null) { "Invalidate runnable is already set." }
        invalidateDataSourceRunnable = runnable
    }

    /**
     * Invalidate the DataSource associated with this notification.
     */
    fun invalidateDataSource() {
        Objects.requireNonNull(invalidateDataSourceRunnable)
        invalidateDataSourceRunnable!!.run()
    }

    companion object {
        /**
         * @return the LiveData observable that emits page load status
         */
        fun getLoadStateObservable(notificationObservable: LiveData<DataSourceNotification>): LiveData<PageLoadState> {
            return Transformations.switchMap(notificationObservable) { n: DataSourceNotification -> n.loadStateObservable }
        }

        /**
         * @return the LiveData observable that emits refresh state
         */
        fun getRefreshStateObservable(notificationObservable: LiveData<DataSourceNotification>): LiveData<PageLoadState> {
            return Transformations.switchMap(notificationObservable) { n: DataSourceNotification -> n.refreshStateObservable }
        }
    }
}
