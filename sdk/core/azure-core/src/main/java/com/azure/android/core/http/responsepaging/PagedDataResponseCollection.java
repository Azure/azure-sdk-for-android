/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Response;
import com.azure.android.core.util.paging.Page;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
     * Constructor requires a page provider that returns page data and response synchronously
     * @param pagedDataRetriever synchronous paged data provider
     */
    public PagedDataResponseCollection(@NonNull PagedDataResponseRetriever<T, P> pagedDataRetriever) {
        Objects.requireNonNull(pagedDataRetriever);
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
        if (firstPageResponse != null
            && firstPageResponse.getValue() != null
            && firstPageResponse.getValue().getPageId() != null) {
            firstPageId = firstPageResponse.getValue().getPageId();
            pages.put(firstPageId, firstPageResponse);
        }
        return  firstPageResponse;
    }

    /**
     * Retrieve a page synchronously along with the response for retrieving it using page id
     * @param pageId id of the page
     * @return page and the response for retrieving it
     */
    public Response<P> getPage(@NonNull String pageId) {
        Objects.requireNonNull(pageId);
        Response<P> pageResponse = pages.get(pageId);
        if (pageResponse != null) {
            return pageResponse;
        }

        pageResponse = pagedDataRetriever.getPage(pageId);
        if (pageResponse != null
            && pageResponse.getValue() != null
            && pageResponse.getValue().getPageId() != null) {
            // setting previous page id should simplify implementation for androidx.arch.DataSource
            final Iterator<Response<P>> iterator = pages.values().iterator();
            while (iterator.hasNext()) {
                final P existingPage = iterator.next().getValue();
                if (pageId.equals(existingPage.getNextPageId())) {
                    pageResponse.getValue().setPreviousPageId(existingPage.getPageId());
                    break;
                }
            }
            pages.put(pageResponse.getValue().getPageId(), pageResponse);
        }
        return pageResponse;
    }
}
