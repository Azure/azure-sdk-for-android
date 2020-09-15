/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import com.azure.android.core.http.SimpleCallback;
import com.azure.android.core.util.paging.Page;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * Represents a collection of pages where page data and response for retrieving it can be returned asynchronously
 * @param <T> type of the items in the page
 * @param <P> Page of items
 */
public class AsyncPagedDataCollection<T, P extends Page<T>> {
    private final AsyncPagedDataRetriever<T, P> pagedDataRetriever;
    private LinkedHashMap<String, Map.Entry<Response, P>> pages = new LinkedHashMap<String, Map.Entry<Response, P>>();
    private String firstPageId;

    /**
     * Construction requires an asynchronous paged data provider
     * @param pagedDataRetriever an asynchronous page data provider
     */
    public AsyncPagedDataCollection(AsyncPagedDataRetriever<T, P> pagedDataRetriever) {
        this.pagedDataRetriever = pagedDataRetriever;
    }

    private void cacheResponse(P page, Response response ){
        pages.put(page.getPageId(), new Map.Entry<Response, P>() {

            @Override
            public Response getKey() {
                return response;
            }

            @Override
            public P getValue() {
                return page;
            }

            @Override
            public P setValue(P newValue) {
                return page;
            }
        });
        // setting previous page id should simplify implementation for androidx.arch.DataSource
        final Iterator<Map.Entry<Response, P>> iterator = pages.values().iterator();
        while(iterator.hasNext()){
            final P existingPage = iterator.next().getValue();
            if (page.getPageId().equals(existingPage.getNextPageId())){
                page.setPreviousPageId(existingPage.getPageId());
                break;
            }
        }
    }

    /**
     * Gets first page in the collection along with the response retrieving the first page
     * @param callback a callback interface for handling the first page and its response
     */
    public void getFirstPage(SimpleCallback<P> callback) {
        if (firstPageId == null){
            pagedDataRetriever.getFirstPage(new SimpleCallback<P>() {

                @Override
                public void onSuccess(P value, Response response) {
                    if (value == null) {
                        callback.onSuccess(value, response);
                        return;
                    }

                    firstPageId = value.getPageId();
                    cacheResponse(value, response);
                    callback.onSuccess(value, response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
        }
        else {
            Map.Entry<Response, P> firstPageResponse = pages.get(firstPageId);
            callback.onSuccess(firstPageResponse.getValue(), firstPageResponse.getKey());
        }
    }

    /**
     * Retrieve a page by its id
     * @param pageId id of the page
     * @param callback callback interface for handling the page along with its response
     */
    public void getPage(String pageId, SimpleCallback<P> callback) {
        Map.Entry<Response, P> pageEntry = pages.get(pageId);
        if (pageEntry == null){
            pagedDataRetriever.getPage(pageId, new SimpleCallback<P>() {
                @Override
                public void onSuccess(P value, Response response) {
                    if (value == null) {
                        callback.onSuccess(null, response);
                    }

                    cacheResponse(value, response);
                    callback.onSuccess(value, response);
                }

                @Override
                public void onFailure(Throwable t, Response response) {
                    callback.onFailure(t, response);
                }
            });
        }
        else {
            callback.onSuccess(pageEntry.getValue(), pageEntry.getKey());
        }
    }
}
