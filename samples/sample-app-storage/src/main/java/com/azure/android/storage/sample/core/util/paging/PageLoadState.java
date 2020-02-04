package com.azure.android.storage.sample.core.util.paging;

/**
 * The page load state.
 */
public enum PageLoadState {
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
    FAILED,
}
