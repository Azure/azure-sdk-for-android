/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.core.http.responsepaging;

import com.azure.core.util.paging.Page;

import java.util.Map;
import java.util.Objects;

import okhttp3.Response;

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
    public abstract Map.Entry<Response, P> getFirstPage();

    /**
     * Get a page by its id
     * @param pageId id of the page
     * @return a Map.Entry that contains both Response and the page
     */
    public abstract Map.Entry<Response, P> getPage(String pageId);
}
