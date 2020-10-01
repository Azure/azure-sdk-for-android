/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a synchronous paged data provider with a fixed page size
 * @param <T> type of the items contained in the page
 * @param <P> type of the page
 */
public abstract class PagedDataRetriever<T, P extends Page<T>> {

    /**
     * Retrieve the first page of the collection
     * @return the first page of the collection
     */
    public abstract P getFirstPage();

    /**
     * Retrieve a page with the given id
     * @param pageId id of the page to retrieve
     * @return a page that matches the given id
     */
    public abstract P getPage(String pageId);
}
