/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

/**
 * Represents a synchronous paged data provider with fixed page size that returns both page and response
 * @param <T> type of the items in the page
 * @param <P> type of the page
 */
public abstract class PagedDataResponseRetriever<T, P extends Page<T>> {

    /**
     * Get first page in the collection
     * @return a response with page data
     */
    public abstract Response<P> getFirstPage();

    /**
     * Get a page by its id
     * @param pageId id of the page
     * @return a response with page data
     */
    public abstract Response<P> getPage(String pageId);
}
