/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.util.paging.Page;

import java.util.Objects;

/**
 * Represents a synchronous paged data provider with fixed page size
 * @param <T> items in the page
 * @param <P> page of items
 */
public abstract class PagedDataRetriever<T, P extends Page<T>> {
    private final int pageSize;

    protected PagedDataRetriever(int pageSize) {
        Objects.requireNonNull(pageSize);
        if (pageSize <= 0)
            throw new IllegalArgumentException("pageSize must be a positive integer");
        this.pageSize = pageSize;
    }

    /**
     * Size of the fixed size page
     * @return size of page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Retrieve first page of the collection. Throws RuntimeException on failure
     * @return the first page of the collection
     */
    public abstract P getFirstPage();

    /**
     * Retrieve page with the given id. Throws RuntimeException on failure
     * @param pageId id of the page to retrieve
     * @return page of items
     */
    public abstract P getPage(String pageId);
}
