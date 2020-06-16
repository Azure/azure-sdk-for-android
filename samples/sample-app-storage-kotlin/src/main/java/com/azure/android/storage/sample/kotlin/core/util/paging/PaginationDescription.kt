package com.azure.android.storage.sample.kotlin.core.util.paging

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

/**
 * A type that exposes Observables to listen for arrival of [PagedList], the page
 * loading status (as user load each page) and page refresh status. This type also
 * expose methods to reload (refresh) the PagedList and retry the last failed page load.
 *
 * @param T the type of items in the page
 */
interface PaginationDescription<T> {
    /**
     * @return the LiveData observable that emits [PagedList]
     */
    val pagedListObservable: LiveData<PagedList<T>>

    /**
     * @return the LiveData observable that emits page load status
     */
    val loadStateObservable: LiveData<PageLoadState>

    /**
     * @return the LiveData observable that emits refresh state when [refresh] is called
     */
    val refreshStateObservable: LiveData<PageLoadState>

    /**
     * Invalidate the last PagedList emitted by [loadStateObservable] and
     * create a new PagedList by retrieving Pages from the beginning, the new PagedList
     * will be emitted by [loadStateObservable].
     */
    fun refresh()

    /**
     * If the last page load was failed then retry it.
     */
    fun retry()
}
