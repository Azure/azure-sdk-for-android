/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.core.http.responsepaging;

import com.azure.core.http.SimpleCallback;
import com.azure.core.util.paging.Page;

import java.util.Objects;

/**
 * Represents an asynchronous paged data provider with fixed page size
 * @param <T> type of the items contained in the page
 * @param <P> page that contains the items
 */
public abstract class AsyncPagedDataRetriever<T, P extends Page<T>> {
    private final int pageSize;

    /**
     * Constructor requires the fixed page size
     * @param pageSize size of the pages
     */
    public AsyncPagedDataRetriever(int pageSize){
        Objects.requireNonNull(pageSize);
        if (pageSize <= 0)
            throw new IllegalArgumentException("pageSize must be a positive integer");
        this.pageSize = pageSize;
    }

    /**
     * Gets the size of the page
     * @return size of the page
     */
    public int getPageSize(){
        return this.pageSize;
    }

    /**
     * Retrieve the first page in the collection
     * @param callback callback function to handle the page data along with the response for retrieving it
     */
    public abstract void getFirstPage(SimpleCallback<P> callback);

    /**
     * Retrieve a page with its id
     * @param pageId id of the page
     * @param callback callback function to handle the page data along with the response for retrieving it
     */
    public abstract void getPage(String pageId, SimpleCallback<P> callback);
}
