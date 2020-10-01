/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.http.responsepaging;

import androidx.annotation.NonNull;

import com.azure.android.core.http.Callback;
import com.azure.android.core.util.paging.Page;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

import okhttp3.Response;

/**
 * Represents a collection of pages where page data and the response it comes with it can be obtained asynchronously
 * @param <T> type of the items in the page
 * @param <P> type of the page
 */
public class AsyncPagedDataCollection<T, P extends Page<T>> {
    private final AsyncPagedDataRetriever<T, P> asyncPagedDataRetriever;
    private LinkedHashMap<String, PageAndResponse<P>> pages = new LinkedHashMap<String, PageAndResponse<P>>();
    private String firstPageId;

    /**
     * Constructor requires an asynchronous paged data provider
     * @param asyncPagedDataRetriever an asynchronous paged data provider
     */
    public AsyncPagedDataCollection(@NonNull AsyncPagedDataRetriever<T, P> asyncPagedDataRetriever) {
        Objects.requireNonNull(asyncPagedDataRetriever);
        this.asyncPagedDataRetriever = asyncPagedDataRetriever;
    }

    private void cacheResponse(P page, Response response) {
        if (page.getPageId() == null) {
            return;
        }
        pages.put(page.getPageId(), new PageAndResponse<P>(page, response));

        // setting previous page id should simplify implementation for androidx.arch.DataSource
        final Iterator<PageAndResponse<P>> iterator = pages.values().iterator();
        while(iterator.hasNext()){
            final P existingPage = iterator.next().page;
            if (page.getPageId().equals(existingPage.getNextPageId())){
                page.setPreviousPageId(existingPage.getPageId());
                break;
            }
        }
    }

    /**
     * Retrieve the first page of the collection along with the response it comes with
     * @param callback callback interface for handling the first page and the response it comes with
     */
    public void getFirstPage(@NonNull Callback<P> callback) {
        Objects.requireNonNull(callback);
        if (firstPageId == null){
            asyncPagedDataRetriever.getFirstPage(new Callback<P>() {
                @Override
                public void onSuccess(P value, Response response) {
                    if (value == null) {
                        callback.onSuccess(null, response);
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
            PageAndResponse<P> firstPageResponse = pages.get(firstPageId);
            callback.onSuccess(firstPageResponse.page, firstPageResponse.response);
        }
    }

    /**
     * Retrieve a page with the given id
     * @param pageId id of the page
     * @param callback callback interface for handling the page along with the response it comes with
     */
    public void getPage(@NonNull String pageId, @NonNull Callback<P> callback) {
        Objects.requireNonNull(pageId);
        Objects.requireNonNull(callback);
        PageAndResponse<P> pageEntry = pages.get(pageId);
        if (pageEntry == null){
            asyncPagedDataRetriever.getPage(pageId, new Callback<P>() {
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
            callback.onSuccess(pageEntry.page, pageEntry.response);
        }
    }

    private static class PageAndResponse<P> {
        public final P page;
        public final Response response;

        public PageAndResponse(P page, Response response) {
            this.page = page;
            this.response = response;
        }
    }
}
