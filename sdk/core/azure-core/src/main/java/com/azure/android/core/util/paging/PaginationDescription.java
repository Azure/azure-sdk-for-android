package com.azure.android.core.util.paging;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

/**
 * A type that exposes Observables to listen for arrival of {@link PagedList}, the page
 * loading status (as user load each page) and page refresh status. This type also
 * expose methods to reload (refresh) the PagedList and retry the last failed page load.
 *
 * @param <T> the type of items in the page
 */
public interface PaginationDescription<T> {
    /**
     * @return the LiveData observable that emits {@link PagedList}
     */
    LiveData<PagedList<T>> getPagedListObservable();

    /**
     * @return the LiveData observable that emits page load status
     */
    LiveData<PageLoadState> getLoadStateObservable();

    /**
     * @return the LiveData observable that emits refresh state when {@link this#refresh()} is called
     */
    LiveData<PageLoadState> getRefreshStateObservable();

    /**
     * Invalidate the last PagedList emitted by {@link this#getLoadStateObservable()} and
     * create a new PagedList by retrieving Pages from the beginning, the new PagedList
     * will be emitted by {@link this#getLoadStateObservable()}.
     */
    void refresh();

    /**
     * If the last page load was failed then retry it.
     */
    void retry();
}
