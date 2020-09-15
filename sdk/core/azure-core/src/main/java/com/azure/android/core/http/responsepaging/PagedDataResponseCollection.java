/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a collection of pages where page and the response for retrieving it can be returned synchronously
 * @param <T> type of the items contained in the page
 * @param <P> page that contains the items
 */
public class PagedDataResponseCollection<T, P extends Page<T>> {
    private final PagedDataResponseRetriever<T, P> pagedDataRetriever;
    private LinkedHashMap<String, Response<P>> pages = new LinkedHashMap<String, Response<P>>();
    private String firstPageId;

    /**
     *  Constructor requires a page provider that returns page data and response synchronously
     * @param pagedDataRetriever synchronous paged data provider
     */
    public PagedDataResponseCollection(PagedDataResponseRetriever<T, P> pagedDataRetriever) {
        this.pagedDataRetriever = pagedDataRetriever;
    }

    /**
     * Retrieves the first page in the collection synchronously
     * @return page data along with the response for retrieving the first page
     */
    public Response<P> getFirstPage() {
        if (firstPageId != null) {
            return pages.get(firstPageId);
        }
        Response<P> firstPageResponse = pagedDataRetriever.getFirstPage();
        pages.put(firstPageResponse.getValue().getPageId(), firstPageResponse);
        return  firstPageResponse;
    }

    /**
     * Retrueve a page synchronously with its id along with the response for retrieving it
     * @param pageId id of the page
     * @return page and the response for retrieving it
     */
    public Response<P> getPage(String pageId) {
        Response<P> page = pages.get(pageId);
        if (page != null) {
            return page;
        }

        page = pagedDataRetriever.getPage(pageId);
        // setting previous page id should simplify implementation for androidx.arch.DataSource
        final Iterator<Response<P>> iterator = pages.values().iterator();
        while(iterator.hasNext()){
            final P existingPage = iterator.next().getValue();
            if (existingPage.getNextPageId().equals(pageId)){
                page.getValue().setPreviousPageId(existingPage.getPageId());
                break;
            }
        }
        pages.put(page.getValue().getPageId(), page);
        return page;
    }
}
