package com.azure.android.storage.sample.kotlin.core.util.paging

/**
 * THE TYPE IS FOR INTERNAL USE. (Consumed only by client-sdk).
 *
 * A contract to fetch page.
 *
 * @param T the type of items in the page
 */
interface PageItemsFetcher<T> {
    /**
     * Fetch a specific page.
     *
     * @param pageIdentifier the identifier of the page
     * @param pageSize the preferred number of items in the page
     * @param callback the callback to notify result of page retrieval
     */
    fun fetchPage(pageIdentifier: String?, pageSize: Int?, callback: FetchCallback<T>)

    /**
     * The callback to notify result of page retrieval
     *
     * @param T the type of items in the page
     */
    interface FetchCallback<T> {
        /**
         * Notify that page retrieval is succeeded.
         *
         * @param items the items in the retrieved page
         * @param currentPageIdentifier the current page identifier
         * @param nextPageIdentifier the next page identifier
         */
        fun onSuccess(items: List<T>, currentPageIdentifier: String?, nextPageIdentifier: String?)

        /**
         * Notify that page retrieval is failed.
         *
         * @param throwable the error received on page retrieval
         */
        fun onFailure(throwable: Throwable?)
    }
}
