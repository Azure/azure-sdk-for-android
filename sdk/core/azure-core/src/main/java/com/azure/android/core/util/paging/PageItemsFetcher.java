package com.azure.android.core.util.paging;

import java.util.List;

/**
 * THE TYPE IS FOR INTERNAL USE. (Consumed only by client-sdk).
 *
 * A contract to fetch page.
 *
 * @param <T> the type of items in the page
 */
public interface PageItemsFetcher<T> {
    /**
     * Fetch a specific page.
     *
     * @param pageIdentifier the identifier of the page
     * @param pageSize the preferred number of items in the page
     * @param callback the callback to notify result of page retrieval
     */
    void fetchPage(String pageIdentifier, Integer pageSize, FetchCallback<T> callback);

    /**
     * The callback to notify result of page retrieval
     *
     * @param <T> the type of items in the page
     */
    interface FetchCallback<T> {
        /**
         * Notify that page retrieval is succeeded.
         *
         * @param items the items in the retrieved page
         * @param currentPageIdentifier the current page identifier
         * @param nextPageIdentifier the next page identifier
         */
        void onSuccess(List<T> items, String currentPageIdentifier, String nextPageIdentifier);

        /**
         * Notify that page retrieval is failed.
         *
         * @param throwable the error received on page retrieval
         */
        void onFailure(Throwable throwable);
    }
}
