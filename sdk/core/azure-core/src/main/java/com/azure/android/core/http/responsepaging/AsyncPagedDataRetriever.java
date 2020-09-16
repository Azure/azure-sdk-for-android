/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Callback;
import com.azure.android.core.util.paging.Page;

/**
 * Represents an asynchronous paged data provider with a fixed page size
 * @param <T> type of the items contained in the page
 * @param <P> type of the page
 */
public abstract class AsyncPagedDataRetriever<T, P extends Page<T>> {

    /**
     * Retrieve the first page in the collection
     * @param callback callback function to handle the page data along with the response it comes with
     */
    public abstract void getFirstPage(Callback<P> callback);

    /**
     * Retrieve a page with the given id
     * @param pageId id of the page
     * @param callback callback function to handle the page data along with the response it comes with
     */
    public abstract void getPage(String pageId, Callback<P> callback);
}
