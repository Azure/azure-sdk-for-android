/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.azure.android.core.util.paging;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a page of items
 * @param <T> type of the items in the page
 */
public class Page<T> {
    private final String pageId;
    private final List<T> items;
    private String nextPageId;
    private String previousPageId;

    /**
     * Constructor requires page id and items
     * @param pageId id of the page
     * @param items items in the page
     */
    public Page(@NonNull String pageId, @NonNull List<T> items) {
        Objects.requireNonNull(pageId);
        Objects.requireNonNull(items);
        this.pageId = pageId;
        this.items = items;
    }

    /**
     * Constructor with page id, items, and next page id
     * @param pageId id of the page
     * @param items items in the page
     * @param nextPageId id of the next page
     */
    public Page(@NonNull String pageId, @NonNull List<T> items, String nextPageId) {
        this(pageId, items);
        this.nextPageId = nextPageId;
    }

    /**
     * Get the page id
     * @return id of the page
     */
    public String getPageId(){
        return this.pageId;
    }

    /**
     * Get the items in the page
     * @return list of items in the page
     */
    public List<T> getItems() {
        return this.items;
    }

    /**
     * Get an iterator for the items in the page
     * @return iterator for the items in the page
     */
    public Iterator<T> getIterator() {
        return items.iterator();
    }

    /**
     * Set the next page id
     * @param nextPageId id of the page after this page
     * @return this page
     */
    public Page<T> setNextPageId(@NonNull String nextPageId) {
        this.nextPageId = nextPageId;
        return this;
    }

    /**
     * Get the next page id
     * @return id of the page after this page
     */
    public String getNextPageId() {
        return this.nextPageId;
    }

    /**
     * Set the previous page id
     * @param previousPageId id of the page before this page
     * @return this page
     */
    public Page<T> setPreviousPageId(@NonNull String previousPageId) {
        this.previousPageId = previousPageId;
        return this;
    }

    /**
     * Get the previous page id
     * @return id of the page before this page
     */
    public String getPreviousPageId() {
        return previousPageId;
    }
}
