/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Represents a collection of paged data with a fixed page size where pages can be retrieved synchronously
 * @param <T> type of the items contained in the page
 * @param <P> type of the page
 */
public class PagedDataCollection<T, P extends Page<T>> {
    private final PagedDataRetriever<T, P> pagedDataRetriever;
    private LinkedHashMap<String, P> pages = new LinkedHashMap<String, P>();
    private String firstPageId;

    /**
     * Constructor that requires a synchronous paged data provider
     * @param pagedDataRetriever paged data provider with a fixed page size
     */
    public PagedDataCollection(@NonNull PagedDataRetriever<T, P> pagedDataRetriever) {
        Objects.requireNonNull(pagedDataRetriever);
        this.pagedDataRetriever = pagedDataRetriever;
    }

    /**
     * Retrieve the first page of the collection
     * @return the first page of the collection
     */
    public P getFirstPage() {
        if (firstPageId != null) {
            return pages.get(firstPageId);
        }
        P firstPage = pagedDataRetriever.getFirstPage();
        this.firstPageId = firstPage.getPageId();
        if (firstPage.getPageId() != null) {
            pages.put(firstPage.getPageId(), firstPage);
        }
        return firstPage;
    }

    /**
     * Retrieve a page with the given id
     * @param pageId id of the page to retrieve
     * @return a page that matches the given id
     */
    public P getPage(@NonNull String pageId) {
        Objects.requireNonNull(pageId);
        P page = pages.get(pageId);
        if (page != null) {
            return page;
        }

        page = pagedDataRetriever.getPage(pageId);
        // setting previous page id should simplify implementation for androidx.arch.DataSource
        final Iterator<P> iterator = pages.values().iterator();
        while(iterator.hasNext()){
            final P existingPage = iterator.next();
            if (pageId.equals(existingPage.getNextPageId())) {
                page.setPreviousPageId(existingPage.getPageId());
                break;
            }
        }
        if (page.getPageId() != null) {
            pages.put(page.getPageId(), page);
        }
        return page;
    }
}
