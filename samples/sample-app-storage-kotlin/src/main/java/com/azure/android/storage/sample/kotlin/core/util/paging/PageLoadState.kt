package com.azure.android.storage.sample.kotlin.core.util.paging

/**
 * The page load state.
 */
enum class PageLoadState {
    /**
     * Page loading is in progress.
     */
    LOADING,

    /**
     * Page loading is completed.
     */
    LOADED,

    /**
     * Page loading is failed.
     */
    FAILED
}
