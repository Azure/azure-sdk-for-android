package com.azure.core.util.paging;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Page<T> {
    private final String pageId;
    private final List<T> items;
    private String nextPageId;
    private String previousPageId;

    public Page(String pageId, List<T> items) {
        Objects.requireNonNull(pageId);
        Objects.requireNonNull(items);
        this.pageId = pageId;
        this.items = items;
    }

    public String getPageId(){
        return this.pageId;
    }

    public List<T> getItems() {
        return this.items;
    }

    public Iterator<T> getIterator() {
        return items.iterator();
    }

    public Page<T> setNextPageId(String nextPageId) {
        this.nextPageId = nextPageId;
        return this;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public Page<T> setPreviousPageId(String previousPageId) {
        this.previousPageId = previousPageId;
        return this;
    }

    public String getPreviousPageId() {
        return previousPageId;
    }
}
