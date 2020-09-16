/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

import java.util.Objects;

/**
 * Represents a synchronized paged data provider with fixed page size that returns both page and response
 * @param <T> type of the items in the page
 * @param <P> type of the page
 */
public abstract class PagedDataResponseRetriever<T, P extends Page<T>> {
    private final int pageSize;

    protected PagedDataResponseRetriever(int pageSize) {
        Objects.requireNonNull(pageSize);
        if (pageSize <= 0)
            throw new IllegalArgumentException("pageSize must be a positive integer");
        this.pageSize = pageSize;
    }

    /**
     * Get the page size
     * @return size of the page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Get first page in the collection
     * @return a Map.Entry that contains both Response and the page
     */
    public abstract Response<P> getFirstPage();

    /**
     * Get a page by its id
     * @param pageId id of the page
     * @return a Response of paged data
     */
    public abstract Response<P> getPage(String pageId);
}
