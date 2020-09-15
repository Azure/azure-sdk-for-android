/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.util.paging;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Represents a collection of paged data with fixed page size where pages can be retrieved synchrounously
 * @param <T> type of the items contained in the page
 * @param <P> page that contains the items
 */
public class PagedDataCollection<T, P extends Page<T>> {
    private final PagedDataRetriever<T, P> pagedDataRetriever;
    private LinkedHashMap<String, P> pages = new LinkedHashMap<String, P>();
    private String firstPageId;

    /**
     * Constructor requires a synchronous paged data provider
     * @param pagedDataRetriever paged data provider with fixed page size
     */
    public PagedDataCollection(PagedDataRetriever<T, P> pagedDataRetriever) {
        this.pagedDataRetriever = pagedDataRetriever;
    }

    /**
     * Retrieve the first page in the collection
     * @return First page of the collection
     */
    public P getFirstPage() {
        if (firstPageId != null) {
            return pages.get(firstPageId);
        }
        P firstPage = pagedDataRetriever.getFirstPage();
        pages.put(firstPage.getPageId(), firstPage);
        return  firstPage;
    }

    /**
     * Retrieve a page with its id
     * @param pageId id of the page
     * @return page of data with the requested id
     */
    public P getPage(String pageId) {
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
        pages.put(page.getPageId(), page);
        return page;
    }
}
