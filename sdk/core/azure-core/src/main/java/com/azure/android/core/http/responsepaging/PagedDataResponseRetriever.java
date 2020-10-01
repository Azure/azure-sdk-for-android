/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

/**
 * Represents a synchronous paged data provider with a fixed page size that returns both page and response
 * @param <T> type of the items contained in the page
 * @param <P> type of the page
 */
public abstract class PagedDataResponseRetriever<T, P extends Page<T>> {

    /**
     * Retrieve the first page of the collection
     * @return a response with page data for the first page of the collection
     */
    public abstract Response<P> getFirstPage();

    /**
     * Retrieve a page with the given id along with the response it comes with
     * @param pageId id of the page to retrieve
     * @return a response with page data for the given page id
     */
    public abstract Response<P> getPage(String pageId);
}
